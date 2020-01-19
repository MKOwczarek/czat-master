package czat.service;
import czat.model.Room;
import czat.model.User;
import czat.room.RoomService;
import czat.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Stream;

@Service
public class CzatServiceImpl implements CzatService {
    private static final Logger logger = LoggerFactory.getLogger(CzatServiceImpl.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Override
    public synchronized void addUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        User user = userService.getUserByUsername(username);

        //add user to room
        if (room.getUsers() == null){
            Set<User> set = new HashSet<>();
            set.add(user);
            room.setUsers(set);
        }else {
            Set<User> oldSet = room.getUsers();
            oldSet.add(user);
            room.setUsers(oldSet);
        }
        roomService.update(room);
        logger.info(username + " connected to the room " + roomid);
    }

    @Override
    public synchronized void removeUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));
        User user = userService.getUserByUsername(username);

        Set<User> usersSet = room.getUsers();
        if (usersSet.size() == 1 && !room.isOpen()){
            room.setUsers(new HashSet<>());
            roomService.delete(room);
            logger.info("delete room " + roomid);
        }else {
            usersSet.remove(user);
            room.setUsers(usersSet);
            roomService.update(room);
            logger.info(username + " left from room " + roomid);
        }
        
    }

    @Override
    public synchronized String getNextUser(String username, String roomid) {
        Room room = roomService.getRoomById(Long.parseLong(roomid));

        List<String> users = new ArrayList<>();
        for (User user : room.getUsers()) {
            users.add(user.getUsername());
        }

        int k = 0;
        while (true){
            if (users.get(k).equals(username)){
                if (k + 1 == users.size()){
                    logger.info("next user is " + users.get(0));
                    return users.get(0);
                }else {
                    logger.info("next user is " + users.get(k+1));
                    return users.get(k+1);
                }
            }
            k++;
        }
    }

    @Override
    public synchronized void clearGuess(String roomid) {
        logger.info("clear guess in room '" + roomid + "'");
        roomService.clearGuess(Long.parseLong(roomid));
    }

    @Override
    public synchronized void setGuess(String roomId, String word) {
        roomService.setWord(Long.parseLong(roomId), word);
    }

}
