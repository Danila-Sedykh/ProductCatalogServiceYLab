package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggerService {

    public void logInfo(String message) {
        Logger logger = LoggerFactory.getLogger("APP_LOGGER");
        logger.info(message);
    }

    public void logError(String message, Throwable t) {
        Logger logger = LoggerFactory.getLogger("APP_LOGGER");
        logger.error(message, t);
    }
}
