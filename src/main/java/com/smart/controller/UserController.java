package com.smart.controller;

import java.io.File;

import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.ContactRepository;
import com.smart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		User user = userRepository.getUserByName(username);
		model.addAttribute("user", user);
	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String AddContact(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("conatct", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByName(name);

			// processing and uploading file
			if (file.isEmpty()) {
				System.out.println("File is empty!!");
				contact.setImage("contact.png");
			} else {
				// upload the file to folder
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				;
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Contact added successfully! Add more", "success"));
			session.removeAttribute("message");
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong! Try again", "danger"));

		}
		return "/normal/add_contact_form";
	}

	@GetMapping("/show-contact/{page}")
	public String viewContact(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "View Contacts");
		String userName = principal.getName();
		User user = this.userRepository.getUserByName(userName);
		Pageable pageable = PageRequest.of(page, 8);
		Page<Contact> contacts = this.contactRepository.findContactsByUserName(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}

	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contaOptional = this.contactRepository.findById(cId);
		Contact contact = contaOptional.get();

		String userName = principal.getName();
		User user = this.userRepository.getUserByName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		else {
			System.out.println("Unauthorised Access!!");
		}

		return "normal/contact_details";
	}
	
	@GetMapping("/delete/{cId}") 
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, HttpSession session) {
	    // Retrieve the contact by id (optional)
	    Optional<Contact> contactOptional = this.contactRepository.findById(cId);
	    Contact contact = contactOptional.get();
	    if (contactOptional.isPresent()) {
	        this.contactRepository.delete(contact);
	        session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
	    } else {
	       session.setAttribute("messsage", new Message("Something went wrong", "danger"));
	    }
	    return "redirect:/user/show-contact/0";
	}
}
