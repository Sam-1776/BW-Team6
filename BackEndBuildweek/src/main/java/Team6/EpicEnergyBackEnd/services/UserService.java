package Team6.EpicEnergyBackEnd.services;


import Team6.EpicEnergyBackEnd.DTO.RoleDTO;
import Team6.EpicEnergyBackEnd.repository.UserRepository;
import Team6.EpicEnergyBackEnd.exceptions.NotFoundException;
import Team6.EpicEnergyBackEnd.models.Role;
import Team6.EpicEnergyBackEnd.models.User;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    UserRepository userDAO;

    @Autowired
    Cloudinary cloudinary;

    public Page<User> getUsers(int pageN, int pageS, String OrderBy) {
        if (pageS > 20) pageS = 20;
        Pageable pageable = PageRequest.of(pageN, pageS, Sort.by(OrderBy));
        return userDAO.findAll(pageable);
    }

    public User findById(UUID id) {
        return userDAO.findById(id).orElseThrow(()-> new NotFoundException(id));
    }

    public User update(UUID id, User userUp) {
        User found = this.findById(id);
        found.setName(userUp.getName());
        found.setSurname(userUp.getSurname());
        found.setEmail(userUp.getEmail());
        found.setPassword(userUp.getPassword());
        return userDAO.save(found);
    }

    public void deleteUser(UUID id) {
        User found = this.findById(id);
        userDAO.delete(found);
    }

    public User findByEmail(String email){
        return userDAO.findByEmail(email).orElseThrow(() -> new NotFoundException("Email "+ email + " non trovata"));
    }

    public User uploadAvatar(User currentUser, MultipartFile image) throws IOException {
        String avatarUrl = (String) cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap()).get("url");
        currentUser.setAvatar(avatarUrl);
        this.userDAO.save(currentUser);
        return currentUser;
    }

    public User updateRole(UUID id, RoleDTO role) throws Exception {
        User found = this.findById(id);
        List<Role> roles = new ArrayList<>();
        roles.addAll(found.getRole());
        if (role.role().toLowerCase().equals(Role.ADMIN.toString().toLowerCase())){
            roles.add(Role.ADMIN);
            found.setRole(roles);
            this.userDAO.save(found);
            return found;
        }else {
            throw new Exception("Invalid role");
        }
    }
}
