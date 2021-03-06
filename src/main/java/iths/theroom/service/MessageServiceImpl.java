package iths.theroom.service;

import iths.theroom.entity.MessageRatingEntity;
import iths.theroom.entity.RoomEntity;
import iths.theroom.entity.UserEntity;
import iths.theroom.exception.BadRequestException;
import iths.theroom.exception.NoSuchUserException;
import iths.theroom.exception.NotFoundException;
import iths.theroom.model.MessageModel;
import iths.theroom.pojos.MessageForm;
import iths.theroom.repository.MessageRepository;
import iths.theroom.entity.MessageEntity;
import iths.theroom.exception.NoSuchMessageException;
import iths.theroom.repository.RoomRepository;
import iths.theroom.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static iths.theroom.factory.MessageFactory.toModel;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private MessageRepository messageRepository;
    private UserRepository userRepository;
    private RoomRepository roomRepository;

    public MessageServiceImpl(){}

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserRepository userRepository, RoomRepository roomRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public MessageModel getMessageByUuid(String uuid) {
        return toModel(messageRepository.findByUuid(uuid).orElseThrow(NoSuchMessageException::new));
    }

    @Override
    public List<MessageModel> getAllMessages() {
        return toModel(messageRepository.findAll());
    }

    @Override
    public MessageModel save(MessageForm form) {
        UserEntity user = userRepository.findByUserName(form.getSender()).orElseThrow(NoSuchUserException::new);
        Optional<RoomEntity> room = roomRepository.getOneByRoomName(form.getRoomName());
        room.orElseThrow(() ->  new BadRequestException("no such room exists"));
        MessageEntity message = new MessageEntity(form.getType(), form.getContent(), user, room.get(), new MessageRatingEntity());
        if(isBanned(user, room.get())){
            message.setContent("ur banned bud");
            return toModel(message);
        }

        room.get().addMessage(message);
        //remove for production
        if(user.getUserName().contains("ban")){
            room.get().banUser(user);
        }
        roomRepository.save(room.get());
        return toModel(messageRepository.save(message));
    }

    @Override
    public void remove(String uuid) {
        Optional<MessageEntity> found = messageRepository.findByUuid(uuid);
        found.ifPresent(messageRepository::delete);
    }

    @Override
    public List<MessageModel> getAllMessagesFromUser(String userName, String roomName, String count) {
        List<MessageEntity> messages = filterByMessagesUsersName(userName);

        if (messages.isEmpty()) {
            throw new NotFoundException("No user found with that username");
        }
        if (roomName != null) {
            try {
                messages = filterByMessagesRoom(messages, roomName);
            } catch (Exception e) {
                throw new NotFoundException("Room not found");
            }
        }
        if (count != null) {
            try {
                int messageCount = Integer.parseInt(count);
                if (messageCount > messages.size()) {
                    throw new NotFoundException("Not enough messages exists for that criteria");
                }
                messages = filterMessagesByCount(messages, messageCount);
            } catch (NumberFormatException e) {
                throw new BadRequestException("Count must be a digit");
            }
        }

        return toModel(messages);
    }

    @Override
    public int decreaseMessageRating(String uuid) {

        Optional<MessageEntity> messageFound = messageRepository.findByUuid(uuid);
        MessageEntity messageEntity = messageFound.orElseThrow(NoSuchMessageException::new);

        messageEntity.getMessageRatingEntity().decreaseRating();
        messageRepository.save(messageEntity);

        return messageEntity.getMessageRatingEntity().getRating();

    }

    @Override
    public int increaseMessageRating(String uuid) {
        Optional<MessageEntity> messageFound = messageRepository.findByUuid(uuid);
        MessageEntity messageEntity = messageFound.orElseThrow(NoSuchMessageException::new);

        messageEntity.getMessageRatingEntity().increaseRating();
        messageRepository.save(messageEntity);

        return messageEntity.getMessageRatingEntity().getRating();

    }

    private List<MessageEntity> filterByMessagesUsersName(String userName) {
        List<MessageEntity> messagesByUser = new ArrayList<>();

        for(MessageEntity messageEntity: messageRepository.findAll()){
            if(messageEntity.getSender().getUserName().equals(userName)){
                messagesByUser.add(messageEntity);
            }
        }
        return messagesByUser;
    }

    private List<MessageEntity> filterByMessagesRoom(List<MessageEntity> messages, String roomName) {
        List<MessageEntity> messagesFilteredByRoomName = new ArrayList<>();

        for (MessageEntity messageEntity : messages) {
            if (messageEntity.getRoomEntity().getRoomName().equals(roomName)) {
                messagesFilteredByRoomName.add(messageEntity);
            }
        }
        return messagesFilteredByRoomName;
    }

    private List<MessageEntity> filterMessagesByCount(List<MessageEntity> messages, int count) {
        List<MessageEntity> messagesByUserLimited = new ArrayList<>();
        int counter = 0;

        for (MessageEntity messageEntity : messages) {
            if(counter  >= count){
                break;
            }
            messagesByUserLimited.add(messageEntity);
            counter++;
        }
        return messagesByUserLimited;
    }
    private boolean isBanned(UserEntity user, RoomEntity room){
        return room.getBannedUsers().contains(user);
    }
}
