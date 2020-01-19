package czat.user;

import czat.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    User getUserByUsername(String username);

    void save(User user);
    void update(User user);
}