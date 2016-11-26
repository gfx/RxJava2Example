package com.github.gfx.rxjava2example;


import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface JsonPlaceholderService {

    @GET("/posts")
    Observable<List<Post>> getPostsAsObservable();

    @GET("/posts/{id}")
    Single<Post> getPostAsSingle(@Path("id") int id);


}
