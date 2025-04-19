package com.example.neura_search.config;

import com.example.neura_search.model.ERole;
import com.example.neura_search.model.Role;
import com.example.neura_search.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (alreadySetup)
            return;

        createRoleIfNotFound(ERole.ROLE_USER);
        createRoleIfNotFound(ERole.ROLE_ADMIN);

        alreadySetup = true;
    }

    private void createRoleIfNotFound(ERole name) {
        if (!roleRepository.findByName(name).isPresent()) {
            roleRepository.save(new Role(name));
        }
    }
}