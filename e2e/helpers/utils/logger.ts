import {createConsola} from 'consola';

export const logger = createConsola({
  level: 4,
  formatOptions: {
    date: false,
    columns: 80,
  },
});

export function debug(message: string, ...args: any[]): void {
  if (process.env.E2E_DEBUG) {
    logger.debug(message, ...args);
  }
}

export function info(message: string, ...args: any[]): void {
  logger.info(message, ...args);
}

export function warn(message: string, ...args: any[]): void {
  logger.warn(message, ...args);
}

export function error(message: string, ...args: any[]): void {
  logger.error(message, ...args);
}
