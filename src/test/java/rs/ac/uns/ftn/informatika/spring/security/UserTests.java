package rs.ac.uns.ftn.informatika.spring.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import rs.ac.uns.ftn.informatika.spring.security.model.Role;
import rs.ac.uns.ftn.informatika.spring.security.model.User;
import rs.ac.uns.ftn.informatika.spring.security.repository.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserTests {
	
	@Autowired
    UserRepository userRepository;

    @Test
    public void test() {

        List<User> listBeforeAdd = new ArrayList<>();
        List<User> listAfterAdd = new ArrayList<>();
        List<User> listBeforeDelete = new ArrayList<>();
        List<User> listFinByLastname = new ArrayList<>();

        Iterable<User> users = userRepository.findAll();
        users.forEach(listBeforeAdd::add);

        User user = userRepository.save(new User("pera", "123", "Pera", "Peric", "pera@gmail.com", true, new Timestamp(System.currentTimeMillis()), new ArrayList<Role>()));;

        users = userRepository.findAll();
        users.forEach(listAfterAdd::add);

        assertThat(listAfterAdd).hasSize(listBeforeAdd.size() + 1);

        users = userRepository.findByLastName("Peric");
        users.forEach(listFinByLastname::add);

        assertThat(listFinByLastname.size() > 1);

        userRepository.deleteById(user.getId());

        users = userRepository.findAll();
        users.forEach(listBeforeDelete::add);

        assertThat(listBeforeDelete).hasSize(listAfterAdd.size() - 1);
    }

}
