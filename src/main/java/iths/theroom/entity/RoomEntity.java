package iths.theroom.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static iths.theroom.config.DataBaseConfig.*;

@Entity(name=TABLE_ROOM)
public class RoomEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name=COLUMN_ROOM_NAME, length=50, nullable=false, unique=true)
    private String roomName;

    @OneToMany(mappedBy = "roomEntity")
    private Set<MessageEntity> messages;

    public RoomEntity(){
        this.messages = new HashSet<>();
    }

    public RoomEntity(String roomName){
        this.roomName = roomName;
        this.messages = new HashSet<>();
    }

    public String getRoomName() {
        return this.roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Set<MessageEntity> getMessages() {
        return this.messages;
    }

    public void setMessages(Set<MessageEntity> messages) {
        this.messages = messages;
    }

    public void addMessage(MessageEntity message){
        this.messages.add(message);
    }

    public void removeMessage(MessageEntity message){
        this.messages.remove(message);
    }
}
