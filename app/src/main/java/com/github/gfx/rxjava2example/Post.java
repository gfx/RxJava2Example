package com.github.gfx.rxjava2example;

// See https://jsonplaceholder.typicode.com/
public class Post {

    long userId;

    long id;

    String title;

    String body;

    @Override
    public String toString() {
        return "Post{id=" + id + '}';
    }
}
