package com.swingy.app;

import com.swingy.controller.AppController;
import com.swingy.persistence.HeroRepository;
import com.swingy.util.RandomProvider;
import com.swingy.view.View;

import javax.validation.Validator;

public record AppContext(View view, HeroRepository repository, RandomProvider randomProvider, Validator validator, AppController controller) {
}
