package io.github.httpmattpvaughn.hnapp.details;

import java.util.List;

import io.github.httpmattpvaughn.hnapp.MainActivityContract;
import io.github.httpmattpvaughn.hnapp.data.Injection;
import io.github.httpmattpvaughn.hnapp.data.StoryRepository;
import io.github.httpmattpvaughn.hnapp.data.model.Story;

/**
 * Created by Matt Vaughn: http://mattpvaughn.github.io/
 */

public class DetailsPresenter implements DetailsContract.Presenter {

    private MainActivityContract.Presenter parentPresenter;
    private DetailsContract.View view;
    private StoryRepository storyManager;
    private Story currentStory;
    private boolean isLoading = false;

    public DetailsPresenter(MainActivityContract.Presenter parentPresenter) {
        this.parentPresenter = parentPresenter;
    }

    @Override
    public void openArticle(Story item) {
        if(!item.isStory()) {
            throw new IllegalArgumentException("Only story items can be opened in article view. Item type = " + item.type);
        }

        this.currentStory = item;
        view.openArticle(item.url);
        view.loadDiscussion(item);
        view.showCommentsLoading();
        view.setArticleViewLock(false);
        loadComments(item);
    }

    @Override
    public void openDiscussion(Story item) {
        this.currentStory = item;
        if (!item.isStory()) {
            // the article is not a story (e.g. it's just a text post or something...)
            view.setArticleViewLock(true);
        } else {
            view.setArticleViewLock(false);
        }
        view.openDiscussion(item.url);
        view.loadDiscussion(item);
        view.showCommentsLoading();
        loadComments(item);
    }

    @Override
    public void closeDetailsPage() {
        parentPresenter.closeDetailsPage();
    }

    @Override
    public Story getCurrentStory() {
        return currentStory;
    }

    @Override
    public void setCurrentStory(Story story) {
        this.currentStory = story;
        if (view != null) {
            loadComments(story);
        }
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void setComments(List<Story> comments) {
        isLoading = false;
        view.addComments(comments);
        view.hideCommentsLoading();
    }

    @Override
    public void openLink(String url) {
        view.openArticle(url);
    }


    // Load all comments and set in the view
    public void loadComments(final Story parentComment) {
        isLoading = true;
        if (storyManager == null) {
            storyManager = Injection.provideStoryRepository();
        }
        // Load comments together
        storyManager.getCommentsList(new StoryRepository.GetCommentsListCallback() {
            @Override
            public void onCommentsLoaded(List<Story> comments, Story parent) {
                if (isLoading) {
                    setComments(comments);
                }
            }
        }, parentComment);
    }

    @Override
    public void addView(DetailsContract.View view) {
        this.view = view;
    }
}
