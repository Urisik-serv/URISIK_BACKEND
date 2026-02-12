package com.urisik.backend.domain.member;

import com.urisik.backend.domain.recipe.entity.Recipe;
import com.urisik.backend.domain.recipe.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Scanner;


/*
@Component
@RequiredArgsConstructor
public class WishNumControl implements CommandLineRunner {

    private final RecipeRepository recipeRepository;

    @Override
    @Transactional
    public void run(String... args) {



        Scanner scanner = new Scanner(System.in);
        long id = scanner.nextLong();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recipe not found: " + id));


        for(int a =0;a<30;a++) {
            recipe.incrementWishCount();
        }
        recipeRepository.save(recipe); // 보통 @Transactional이면 save 없어도 flush 시 반영됨
        System.out.println("done");
    }
}


 */