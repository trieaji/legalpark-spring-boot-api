package com.soloproject.LegalPark.helper;

import com.soloproject.LegalPark.entity.Users;
import com.soloproject.LegalPark.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class InfoAccount {

    @Autowired
    UsersRepository usersRepository;


    public Users get(){

        var auth =  SecurityContextHolder.getContext().getAuthentication();
        var name = auth.getName();
        var data = usersRepository.findByEmail(name);

        if(data.isEmpty()){
            return null;
        }

        return data.get();

    }
}
