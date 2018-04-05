package org.eltex.softwlc.stacollector.wshandler;

import org.eltex.softwlc.stacollector.entity.StaData;
import org.eltex.softwlc.stacollector.repo.StaDataRepo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WsHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private StaDataRepo staDataDao;

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        //        super.handleTextMessage(session, message);
        logger.debug("Handling text message: " + message.getPayload());
        ObjectMapper objectMapper = new ObjectMapper();
        StaData data;
        try {
            data = objectMapper.readValue(message.getPayload(), StaData.class);
        } catch (Exception e) {
            session.sendMessage(new TextMessage("Failed"));
            logger.error("Error in handling text message: " + e);
            return;
        }
        if (data != null)
            staDataDao.insert(data);

        session.sendMessage(new TextMessage("OK"));
    }

}
