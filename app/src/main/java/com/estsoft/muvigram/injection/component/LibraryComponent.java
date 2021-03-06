package com.estsoft.muvigram.injection.component;

import com.estsoft.muvigram.injection.module.LibraryModule;
import com.estsoft.muvigram.injection.scope.LibraryScope;
import com.estsoft.muvigram.ui.library.LibraryActivity;
import com.estsoft.muvigram.ui.library.musiclibrary.injection.MusicLibraryComponent;
import com.estsoft.muvigram.ui.library.musiclibrary.injection.MusicLibraryModule;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryComponent;
import com.estsoft.muvigram.ui.library.videolibrary.injection.VideoLibraryModule;

import dagger.Component;

/**
 *
 * Created by jaylim on 10/01/2017.
 */

@LibraryScope
@Component(dependencies = ActivityComponent.class, modules = LibraryModule.class)
public interface LibraryComponent {
  /* Subcomponent */
  MusicLibraryComponent plus(MusicLibraryModule musicLibraryModule);
  VideoLibraryComponent plus(VideoLibraryModule videoLibraryModule);

  /* Dependency objects extended by constructor injections */


  /* Dependency objects provided from modules and dependencies */


  /* Field injection */
  void inject(LibraryActivity libraryActivity);


}
