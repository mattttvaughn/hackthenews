package io.github.httpmattpvaughn.hnapp;

import android.os.Bundle;

import java.util.List;

import io.github.httpmattpvaughn.hnapp.data.model.Story;
import io.github.httpmattpvaughn.hnapp.details.DetailsContract;
import io.github.httpmattpvaughn.hnapp.frontpage.FrontPageContract;

/**
 * Created by Matt Vaughn: http://mattpvaughn.github.io/
 */

public interface MainActivityContract {
    // The *view* part of model-view-presenter
    interface View {
        // Opens an article
        void openDetailsPage();

        // Returns to the top stories page
        void closeDetailsPage();

        // Checks if the details page is open
        boolean isDetailsPageOpen();

        // Asks the user if they wish to quit the app
        void promptQuit();
    }

    // the *presenter* part of model-view-presenter
    interface Presenter {
        // Open an article in the webview
        void openArticle(Story story);

        // Open an article to the discussion page
        void openDiscussion(Story story);

        void addDetailsPresenter(DetailsContract.Presenter detailsPresenter);

        void addFrontPagePresenter(FrontPageContract.Presenter frontPagePresenter);

        // Returns to the top stories page
        void closeDetailsPage();

        // Get the currently opened story- returns null if no stories have been
        // opened or if app is open to front page
        Story getCurrentStory();

        // Attaches to a view
        void attachView(MainActivityContract.View view);

        // Remove reference to the view
        void detachView();

        // Sets the comments in the details view
        void setComments(List<Story> comments);

        // Handles serialized data after activity rebuild
        void parseSavedInstanceState(Bundle savedInstanceState);

        // Save current state to a bundle so it can be restored if needed
        void bundleSavedInstanceState(Bundle outState);

        //
        void restoreStory();
    }
}
