package io.github.httpmattpvaughn.hnapp.frontpage;

import io.github.httpmattpvaughn.hnapp.MainActivityContract;
import io.github.httpmattpvaughn.hnapp.data.Injection;
import io.github.httpmattpvaughn.hnapp.data.StoryRepository;
import io.github.httpmattpvaughn.hnapp.data.model.Story;

/**
 * Created by Matt Vaughn: http://mattpvaughn.github.io/
 */

public class FrontPagePresenter implements FrontPageContract.Presenter {

    private MainActivityContract.Presenter parentPresenter;
    private FrontPageContract.View view;
    private StoryRepository storyRepository;
    private boolean isStoryListLoaded = false;

    public FrontPagePresenter(MainActivityContract.Presenter parentPresenter) {
        this.parentPresenter = parentPresenter;
    }

    @Override
    public void loadStories() {
        System.out.println("Loading stories");
        if (storyRepository == null) {
            storyRepository = Injection.provideStoryRepository();
        }
        if (!isStoryListLoaded) {
            // Load the list that contains links to all of the top 400 posts
            storyRepository.getStoryIdArray(storyIds -> {
                isStoryListLoaded = true;

                // Load the next 25 stories
                storyRepository.getStoryList(stories -> {
                    if (stories == null || stories.isEmpty()) {
                        view.showErrorMessage("Error loading stories. Maybe you've reached the end?");
                        return;
                    }
                    view.setRefreshing(false);
                    view.addStories(stories);
                });
            });
        } else {
            storyRepository.getStoryList(stories -> {
                view.setRefreshing(false);
                view.addStories(stories);
            });
        }
    }

    @Override
    public void reloadStories() {
        view.clearStories();
        resetStoriesLoadedCount();
        loadStories();
    }

    @Override
    public void resetStoriesLoadedCount() {
        if (storyRepository == null) {
            storyRepository = Injection.provideStoryRepository();
        }
        storyRepository.resetStoriesLoadedCount();
    }

    @Override
    public void openArticle(Story story) {
        assert story.isStory();
        parentPresenter.openArticle(story);
    }

    @Override
    public void openDiscussion(Story story) {
        parentPresenter.openDiscussion(story);
    }

    @Override
    public void detachView() {
        this.view = null;
    }

    @Override
    public void attachView(FrontPageContract.View view) {
        this.view = view;
    }
}
