import React from 'react';

import { FaGithub } from 'react-icons/fa';
import { useTranslation } from 'react-i18next';
import Typography from '../Typography/Typography';
import DiscordIcon from './FooterIcons/DiscordIcon';
import FacebookIcon from './FooterIcons/FacebookIcon';

type FooterProps = Record<string, never>;

const Footer: React.FC<FooterProps> = () => {
  const { t } = useTranslation();

  return (
    <>
      <footer className="m-auto mt-16 flex max-w-[1328px] flex-col-reverse items-center gap-6 rounded-t-lg bg-primary-50 px-14 py-12 dark:border-x dark:border-t dark:border-primary-900 dark:bg-primary-800/20 md:flex-row md:justify-between md:py-12">
        <div className="flex h-28 flex-col items-center text-primary-950 dark:text-primary-50 md:justify-between">
          <Typography component="h1" variant="h2">
            Mu Online
          </Typography>
          <Typography variant="label2-r" styles="text-center">
            {t('footer.copyright1')} <br></br> {t('footer.copyright2')}
          </Typography>
          <span className="flex gap-1 font-inter text-[12px] text-primary-950 dark:text-primary-50">
            {t('footer.developedBy')}
            <a
              className="flex items-center gap-1 hover:text-primary-800 dark:hover:text-primary-200"
              href="https://github.com/felipeemerson"
              target="_blank"
            >
              <FaGithub className="size-3" /> felipeemerson
            </a>
          </span>
        </div>
        <div className="flex place-items-center gap-6">
          <a
            href="https://discord.com/channels/1475582393082450000/1475582393971900529"
            target="_blank"
            className="flex h-12 w-12 cursor-pointer items-center justify-center rounded-md bg-primary-500 text-white hover:bg-primary-600 dark:hover:bg-primary-400 sm:h-14 sm:w-14"
          >
            <DiscordIcon />
          </a>
          <a
            href="https://www.facebook.com/profile.php?id=61582192319397"
            target="_blank"
            className="flex h-12 w-12 cursor-pointer items-center justify-center rounded-md bg-primary-500 text-white hover:bg-primary-600 dark:hover:bg-primary-400 sm:h-14 sm:w-14"
          >
            <FacebookIcon />
          </a>
        </div>
      </footer>
    </>
  );
};

export default Footer;
