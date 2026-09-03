package com.adflow.sample;

import com.adflow.content.Content;
import com.adflow.content.ContentRepository;
import com.adflow.user.User;
import com.adflow.user.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SampleDataLoader implements ApplicationRunner {

    private final UserRepository users;
    private final ContentRepository contents;

    public SampleDataLoader(UserRepository users, ContentRepository contents) {
        this.users = users;
        this.contents = contents;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (users.count() == 0) {
            users.save(new User(1L, 28, "스포츠"));
            users.save(new User(2L, 45, "드라마"));
        }
        if (contents.count() == 0) {
            contents.save(new Content(1L, "축구 하이라이트", "스포츠"));
            contents.save(new Content(2L, "로맨스 드라마", "드라마"));
        }
    }
}
