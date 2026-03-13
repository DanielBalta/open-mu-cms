package io.github.felipeemerson.openmuapi.services;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.JsonSyntaxException;
import io.github.felipeemerson.openmuapi.configuration.SystemConstants;
import io.github.felipeemerson.openmuapi.dto.CharacterRankDTO;
import io.github.felipeemerson.openmuapi.dto.GameServerInfoDTO;
import io.github.felipeemerson.openmuapi.dto.OnlinePlayersDTO;
import io.github.felipeemerson.openmuapi.dto.ServerStatisticsDTO;
import io.github.felipeemerson.openmuapi.entities.Account;
import io.github.felipeemerson.openmuapi.entities.GameConfiguration;
import io.github.felipeemerson.openmuapi.entities.GameServerDefinition;
import io.github.felipeemerson.openmuapi.entities.GameServerEndpoint;
import io.github.felipeemerson.openmuapi.enums.AccountState;
import io.github.felipeemerson.openmuapi.exceptions.BadRequestException;
import io.github.felipeemerson.openmuapi.exceptions.ForbiddenException;
import io.github.felipeemerson.openmuapi.repositories.GameConfigurationRepository;
import io.github.felipeemerson.openmuapi.repositories.GameServerDefinitionRepository;
import io.github.felipeemerson.openmuapi.repositories.GameServerEndpointRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class GameServerService {

    private final GameServerDefinitionRepository serverDefinitionRepository;
    private final GameServerEndpointRepository endpointRepository;
    private final GameConfigurationRepository gameConfigurationRepository;
    private final AccountService accountService;
    private final CharacterService characterService;
    private HttpEntity<?> adminApiClient;

    @Value("${admin.panel.username}")
    private String adminPanelUsername;

    @Value("${admin.panel.password}")
    private String adminPanelPassword;

    private final RestTemplate restTemplate;

    public GameServerService(@Autowired GameServerDefinitionRepository serverDefinitionRepository,
                             @Autowired GameServerEndpointRepository endpointRepository,
                             @Autowired GameConfigurationRepository gameConfigurationRepository,
                             @Autowired AccountService accountService,
                             @Autowired CharacterService characterService,
                             @Autowired RestTemplate restTemplate) {

        this.serverDefinitionRepository = serverDefinitionRepository;
        this.endpointRepository = endpointRepository;
        this.gameConfigurationRepository = gameConfigurationRepository;
        this.accountService = accountService;
        this.characterService = characterService;
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void setUpAdminApiClient() {
        String authStr = String.format("%s:%s", adminPanelUsername, adminPanelPassword);
        String base64Creds = Base64.getEncoder().encodeToString(authStr.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        this.adminApiClient = new HttpEntity<>(headers);
    }

    public List<GameServerInfoDTO> getGameServerInfo() {
        List<GameServerDefinition> serverDefinitions = serverDefinitionRepository.findAll();
        return serverDefinitions.stream().map(serverDefinition -> {
            GameServerEndpoint endpoint = endpointRepository.findByGameServerDefinitionId(serverDefinition.getId());
            return new GameServerInfoDTO(
                    serverDefinition.getServerId(),
                    endpoint.getNetworkPort(),
                    serverDefinition.getDescription(),
                    serverDefinition.getExperienceRate(),
                    serverDefinition.getGameConfigurationId()
            );
        }).collect(Collectors.toList());
    }

    public GameConfiguration getGameConfiguration() {
        return gameConfigurationRepository.findFirstBy();
    }

    public OnlinePlayersDTO getOnlinePlayers() {
        String url = SystemConstants.ADMIN_PANEL_URL + SystemConstants.ONLINE_PLAYERS_ENDPOINT;
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    adminApiClient,
                    String.class);
        } catch (RestClientException e) {
            log.error("Error calling admin panel for online players at URL: {}", url, e);
            return new OnlinePlayersDTO();
        }

        if (response == null || response.getStatusCode().isError()) {
            log.error("Admin panel returned an error status or null response for online players. Status: {}, Body: {}",
                    response != null ? response.getStatusCode() : "null", response != null ? response.getBody() : "null");
            return new OnlinePlayersDTO();
        }

        try {
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                log.warn("Admin panel returned an empty or null body for online players at URL: {}", url);
                return new OnlinePlayersDTO();
            }
            return new Gson().fromJson(body, OnlinePlayersDTO.class);
        } catch (JsonSyntaxException e) {
            log.error("Error parsing JSON response from admin panel for online players. Body: {}, URL: {}", response.getBody(), url, e);
            return new OnlinePlayersDTO();
        }
    }

    public boolean isAccountOnline(String loginName) {
        String url = String.format("%s%s/%s", SystemConstants.ADMIN_PANEL_URL, SystemConstants.IS_ACCOUNT_ONLINE_ENDPOINT, loginName);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    adminApiClient,
                    String.class);
        } catch (RestClientException e) {
            log.error("Error calling admin panel to check if account {} is online at URL: {}", loginName, url, e);
            return false;
        }

        if (response == null || response.getStatusCode().isError()) {
            log.error("Admin panel returned an error status or null response for account online status. Account: {}, Status: {}, Body: {}",
                    loginName, response != null ? response.getStatusCode() : "null", response != null ? response.getBody() : "null");
            return false;
        }

        String body = response.getBody();
        if (body == null || body.isEmpty()) {
            log.warn("Admin panel returned an empty or null body for account online status. Account: {}, URL: {}", loginName, url);
            return false;
        }
        return Boolean.parseBoolean(body);
    }

    public List<CharacterRankDTO> getOnlinePlayersDetailed() {
        OnlinePlayersDTO onlinePlayers = this.getOnlinePlayers();
        if (onlinePlayers == null || onlinePlayers.getPlayersList() == null) {
            return Collections.emptyList();
        }
        return this.characterService.getPlayersByName(Arrays.asList(onlinePlayers.getPlayersList()));
    }

    public void sendServerMessage(String message, int serverId, String loginName) {
        Account account = this.accountService.getAccountByLoginName(loginName);

        if (!account.getState().equals(AccountState.GAME_MASTER)) {
            throw new ForbiddenException("Restrict access");
        }

        int numberOfServers = this.getGameServerInfo().size();

        if (serverId < 0 || serverId >= numberOfServers) {
            throw new BadRequestException("Server id invalid.");
        }

        String url = String.format("%s%s/%s?msg=%s", SystemConstants.ADMIN_PANEL_URL, SystemConstants.SEND_MESSAGE_ENDPOINT, serverId, message);
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    adminApiClient,
                    String.class
            );
        } catch (RestClientException e) {
            log.error("Error calling admin panel to send message '{}' to server {} for account {} at URL: {}", message, serverId, loginName, url, e);
            return;
        }


        if (response == null || response.getStatusCode().isError()){
            log.error("Admin panel returned an error status or null response when sending message. Message: '{}', ServerId: {}, Account: {}, Status: {}, Body: {}",
                    message, serverId, loginName, response != null ? response.getStatusCode() : "null", response != null ? response.getBody() : "null");
        }
    }

    public ServerStatisticsDTO getStatistics() {
        ServerStatisticsDTO serverStatisticsDTO = this.gameConfigurationRepository.getStatistics();

        OnlinePlayersDTO onlinePlayers = this.getOnlinePlayers();
        serverStatisticsDTO.setOnlines(onlinePlayers != null ? onlinePlayers.getPlayers() : 0);

        return serverStatisticsDTO;
    }

 }
