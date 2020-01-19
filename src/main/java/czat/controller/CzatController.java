package czat.controller;
import czat.model.Room;
import czat.model.User;
import czat.room.RoomService;
import czat.user.UserService;
import czat.validator.RoomValidator;
import czat.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
public class CzatController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserValidator userValidator;

    @Autowired
    private RoomValidator roomValidator;

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/logout", method=RequestMethod.GET)
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @RequestMapping(value = {"/login", "/"}, method = RequestMethod.GET)
    public String loginPage(){
        Object securityContext = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (securityContext == "anonymousUser"){
            return "login";
        }
        return "redirect:/index";
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(Model model, Principal principal){
        
        String username = principal.getName();
        if (username == null || username.isEmpty()){
            return "redirect:/login";
        }

        List<Room> rooms = roomService.getAll();
        model.addAttribute("listRooms", rooms);
        model.addAttribute("username", username);

        return "index";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.GET)
    public String registrationForm(Model model){
        Object securityContext = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (securityContext == "anonymousUser"){
            model.addAttribute("user", new User());
            return "registration";
        }
        return "redirect:/index";
    }

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registerUserAccount(@ModelAttribute("user") User user, BindingResult result){
        userValidator.validate(user, result);
        if (result.hasErrors()){
            return "registration";
        }
        userService.save(user);
        return "redirect:/login";
    }

    @RequestMapping(value = "/room/add", method = RequestMethod.GET)
    public String roomAddForm(Model model){
        model.addAttribute("room", new Room());
        return "room-add";
    }

    @RequestMapping(value = "/room/add", method = RequestMethod.POST)
    public String addRoom(@ModelAttribute("room") Room room, BindingResult result){
        roomValidator.validate(room, result);
        if (result.hasErrors()){
            return "room-add";
        }
        roomService.save(room);
        return "redirect:/index";
    }

    @RequestMapping(value = "/room/edit", method = RequestMethod.GET)
    public String roomEditForm(@RequestParam(name = "roomId") long roomId, Model model){
        Room room = roomService.getRoomById(roomId);
        model.addAttribute("room", room);
        return "room-edit";
    }

    @RequestMapping(value = "/room/edit", method = RequestMethod.POST)
    public String updateRoom(@RequestParam(name = "roomId") long roomId, Room room, BindingResult result){
        roomValidator.validate(room, result);
        if (result.hasErrors()){
            return "room-edit";
        }
        roomService.update(room, roomId);
        return "redirect:/index";
    }
}
