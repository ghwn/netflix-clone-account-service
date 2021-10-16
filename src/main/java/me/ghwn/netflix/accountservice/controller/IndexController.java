package me.ghwn.netflix.accountservice.controller;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {

    @GetMapping("/api")
    public ResponseEntity<?> index() {
        RepresentationModel<?> content = RepresentationModel.of(null);
        content.add(Link.of("/docs/index.html#resources-index-access").withRel("profile"));
        content.add(linkTo(AccountController.class).withRel("accounts"));
        return ResponseEntity.ok(content);
    }
}
