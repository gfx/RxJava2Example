package com.github.gfx.rxjava2example;

// See https://jsonplaceholder.typicode.com/
public class Post {

    public long userId;

    public long id;

    public String title;

    public String body;

    @Override
    public String toString() {
        return "Post{id=" + id + '}';
    }
}
