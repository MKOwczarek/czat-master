package czat.service;

import org.springframework.stereotype.Service;

@Service
public interface CzatService {
    void addUser(String username, String roomid);
    void removeUser(String username, String roomid);
    void setGuess(String roomId, String word);
    void clearGuess(String roomid);
    String getNextUser(String username, String roomid);
}