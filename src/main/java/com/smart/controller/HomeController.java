package com.smart.controller;

import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.repository.UserRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// Handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,
	                           BindingResult result,
	                           @RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
	                           Model model,
	                           HttpSession session) {
	    if (result.hasErrors()) {
	        // Validation errors
	        model.addAttribute("user", user);
	        session.setAttribute("message", new Message("Validation errors occurred!", "alert-danger"));
	        return "signup";
	    }

	    try {
	        if (!agreement) {
	            throw new Exception("You have not agreed to the terms and conditions");
	        }

	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setImageUrl("default.png");
	        user.setPassword(passwordEncoder.encode(user.getPassword()));

	        this.userRepository.save(user);
	        model.addAttribute("user", new User());
	        session.setAttribute("message", new Message("Registered Successfully!!", "alert-success"));
	        return "signup";
	    } catch (Exception e) {
	        e.printStackTrace();
	        model.addAttribute("user", user);
	        session.setAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert-danger"));
	        return "signup";
	    }
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login Page");
		return "login";
	}
}
