package iths.theroom.service;

import iths.theroom.model.MessageModel;
import iths.theroom.pojos.MessageForm;

import java.util.List;

public interface MessageService {

    MessageModel getMessageByUuid(String uuid);
    List<MessageModel> getAllMessages();
    MessageModel save(MessageForm form);
    void remove(String uuid);
    List<MessageModel> getAllMessagesFromUser(String userName, String roomName, String count);

    int decreaseMessageRating(String uuid);

    int increaseMessageRating(String uuid);
}
