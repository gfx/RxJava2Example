package com.github.gfx.rxjava2example;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.model.Statement;

import io.reactivex.Observable;

@RunWith(JUnit4.class)
public class RxExampleOnJvm {

    @Rule
    public final TestRule testRune = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            System.out.println(description.getDisplayName());
            base.evaluate();
        }
    };

    @Test
    public void take3ForUnlimitedRange() throws Exception {
        Observable.range(0, Integer.MAX_VALUE)
                .take(3)
                .forEach(System.out::println);
    }


    @Test
    public void merge() throws Exception {
        Observable.merge(Observable.just(1, 2), Observable.just(10, 20))
                .forEach(System.out::println);
    }

    @Test
    public void sorted() throws Exception {
        Observable.just(1, 3, 2, 4)
                .sorted()
                .forEach(System.out::println);
    }

}