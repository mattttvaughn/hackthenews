package io.github.httpmattpvaughn.hnapp.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.httpmattpvaughn.hnapp.data.model.Story;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Matt Vaughn: http://mattpvaughn.github.io/
 */

public class StoryManager implements StoryRepository {

    private Integer[] storyIdArr;

    // Number of stories to be loaded at a time
    private static final int STORIES_PER_PAGE = 25;

    // The number of stories that have been loaded so far
    private int storiesLoadedCount = 0;

    @Override
    public void getStoryIdArray(@NonNull final GetStoryIdsCallback callback) {
        Call<Integer[]> call = HackerNewsService.retrofit.topStories();
        call.enqueue(new Callback<Integer[]>() {
            @Override
            public void onResponse(@NonNull Call<Integer[]> call, @NonNull Response<Integer[]> response) {
                storyIdArr = response.body();
                callback.onPostsLoaded(storyIdArr);
            }

            @Override
            public void onFailure(@NonNull Call<Integer[]> call, @NonNull Throwable t) {
                Log.e("HNapp", "Error loading story list.");
                callback.onPostsLoaded(null);
            }
        });
    }

    @Override
    public void getStoryList(@NonNull final GetStoryListCallback callback) {
        System.out.println("Getting story list");
        final List<Story> storyList = new ArrayList<>();
        if (storyIdArr == null) {
            return;
        }
        for (int i = storiesLoadedCount; i < storiesLoadedCount + STORIES_PER_PAGE; i++) {
            // HN only allows us to load up to 500 stories, so return an empty
            // list to indicate to the presenter that there are no more stories
            if (i > storyIdArr.length) {
                callback.onPostsLoaded(new ArrayList<>());
                return;
            }
            Call<Story> call = HackerNewsService.retrofit.item(storyIdArr[i]);
            call.enqueue(new Callback<Story>() {
                @Override
                public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                    Story story = response.body();
                    storyList.add(story);
                    storiesLoadedCount++;

                    // Only send to presenter when we have received responses
                    // for all stories
                    if (storiesLoadedCount == STORIES_PER_PAGE) {
                        callback.onPostsLoaded(storyList);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Story> call, @NonNull Throwable t) {
                    // Still increment the count because we still want to return
                    // the stories, even if some fail to load
                    storiesLoadedCount++;
                    if (storiesLoadedCount == STORIES_PER_PAGE) {
                        callback.onPostsLoaded(storyList);
                    }
                    Log.e("HNapp", "Error loading story. " + t);
                }
            });
        }
    }

    @Override
    public void getCommentsList(@NonNull final GetCommentsListCallback callback,
                                final Story parent) {
        // check in cache for comments list with ID equal to this parent
        List<Story> thread = CommentsCache.getThread(parent.id);
        if (thread != null) {
            callback.onCommentsLoaded(thread, parent);
        } else {
//            Observable.just(true)
//                    .subscribeWith(new DisposableObserver<Boolean>() {
//                        @Override
//                        public void onNext(Boolean aBoolean) {
//
//                        }
//
//                        @Override
//                        public void onError(Throwable e) {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//
//                        }
//                    }).subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe();
            FetchCommentsTask task = new FetchCommentsTask();
            task.execute(new ArrayList<>(), parent, callback);
        }
    }

    // Call callback in depth levels- load root comments, then their children, then their children's children, etc.
    private static class FetchCommentsTask extends AsyncTask<Object, Object, List<Story>> {
        private GetCommentsListCallback callback;
        private Story parent;

        @Override
        protected void onPostExecute(List<Story> stories) {
            super.onPostExecute(stories);
            System.out.println("Stories loaded " + stories);
            callback.onCommentsLoaded(stories, parent);
            CommentsCache.addThread(stories, parent.id);
        }

        @Override
        protected List<Story> doInBackground(Object[] objects) {
            List<Story> comments = (List<Story>) objects[0];
            parent = (Story) objects[1];
            callback = (GetCommentsListCallback) objects[2];
            return getAllComments(parent, comments, 0);
        }

    }

    public void getFakeCommentsIndividually(LoadCommentsIndividuallyCallback callback) {
        List<Story> stories = getFakeStories();

        int FAKE_CHILDREN_PER_LEVEL = 100;
        int LEVELS = 3;

        for (int j = 0; j < LEVELS; j++) {
            List<Story> childStories = new ArrayList<>();
            List<Story> matchingParents = new ArrayList<>();
            for (int i = 1; i <= FAKE_CHILDREN_PER_LEVEL; i++) {
                Story child = stories.get(j * FAKE_CHILDREN_PER_LEVEL + i);
                int parentPosition = Math.max(j - 1, 0) * FAKE_CHILDREN_PER_LEVEL + i;
                Story parent;
                if (parentPosition == 0) {
                    parent = stories.get(0);
                } else {
                    parent = stories.get(parentPosition);
                }
                childStories.add(child);
                matchingParents.add(parent);
            }
            callback.onCommentsLoad(childStories, matchingParents);
        }
    }

    private static List<Story> getFakeStories() {
        List<Story> stories = new ArrayList<>();

        int FAKE_CHILDREN_PER_LEVEL = 100;
        int LEVELS = 3;

        Story root = new Story();
        root.id = 0;
        root.kids = new int[FAKE_CHILDREN_PER_LEVEL];

        for (int i = 0; i < FAKE_CHILDREN_PER_LEVEL; i++) {
            root.kids[i] = i + 1;
        }

        stories.add(root); // index 0

        for (int j = 0; j < LEVELS; j++) {
            for (int i = 1; i <= FAKE_CHILDREN_PER_LEVEL; i++) {
                Story newStory = new Story();
                newStory.id = j * FAKE_CHILDREN_PER_LEVEL + i;
                newStory.parent = Math.max(j - 1, 0) * FAKE_CHILDREN_PER_LEVEL + i;
                newStory.kids = new int[]{(j + 1) * FAKE_CHILDREN_PER_LEVEL + i};
                newStory.by = "level " + (j + 1);
                newStory.depth = j + 1;
                stories.add(newStory); // indices 1-900
            }
        }

        return stories;
    }

    @Override
    public void loadCommentsIndividually(@NonNull final LoadCommentsIndividuallyCallback callback,
                                         final Story parent,
                                         final int depth) {
        final List<Story> comments = new ArrayList<>();
        if (parent == null) {
            return;
        }
        if (parent.kids == null || parent.kids.length == 0) {
            return;
        }
        for (int i = 0; i < parent.kids.length; i++) {
            final int childCommentId = parent.kids[i];
            Call<Story> call = HackerNewsService.retrofit.item(childCommentId);
            call.enqueue(new Callback<Story>() {
                @Override
                public void onResponse(@NonNull Call<Story> call, @NonNull Response<Story> response) {
                    Story childComment = response.body();
                    if (childComment != null) {
                        childComment.depth = depth;
                        int parentPosition = comments.indexOf(parent);
                        comments.add(parentPosition + 1, childComment);
                        // Recursively load children comments of this comment
                        loadCommentsIndividually(callback, childComment, depth + 1);
                    }
                }

                @Override
                public void onFailure(Call<Story> call, Throwable t) {
                    Log.e("HNapp", "Unable to load comment with id " + childCommentId);
                }
            });
        }
        // depth > 0 so we don't include the root story (the post) as a comment
        if (depth > 0) {
//            callback.onCommentsLoaded(comments, parent);
        }
    }

    // Synchronously get comments
    public static List<Story> getAllComments(Story parent, List<Story> comments, int depth) {
        if (parent.kids == null || parent.kids.length == 0) {
            return comments;
        }
        for (int i = 0; i < parent.kids.length; i++) {
            final int childCommentId = parent.kids[i];
            try {
                Response response = HackerNewsService.retrofit.item(childCommentId).execute();
                Story childComment = (Story) response.body();
                if (childComment != null) {
                    parent.addChild(childComment);
                    childComment.depth = depth;
                    int parentPosition = comments.indexOf(parent);
                    comments.add(parentPosition + 1, childComment);
                    // Recursively load children comments of this comment
                    if (childComment.kids != null && childComment.kids.length != 0) {
                        getAllComments(childComment, comments, depth + 1);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return comments;
    }

    public void resetStoriesLoadedCount() {
        this.storiesLoadedCount = 0;
    }

    private static class CommentsCache {
        private final static int THREADS_TO_CACHE = 200;
        private static List<List<Story>> cache = new ArrayList<>();
        private static List<Integer> parentIds = new ArrayList<>();

        static void addThread(List<Story> storyList, int parentId) {
            if (cache.size() >= THREADS_TO_CACHE) {
                // remove last item from cache if no space
                cache.remove(cache.size() - 1);
                parentIds.remove(cache.size() - 1);
            }
            parentIds.add(parentId);
            cache.add(storyList);
        }

        static List<Story> getThread(int parentId) {
            int position = parentIds.indexOf(parentId);
            if (position < 0) {
                return null;
            }
            List<Story> thread = cache.get(position);

            // bump the thread up to the top of the list
            cache.remove(thread);
            cache.add(thread);
            parentIds.remove(position);
            parentIds.add(parentId);

            return thread;
        }
    }

}
