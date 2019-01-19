package com.codebosses.flicks.fragments.tvfragments.tvshowsdetailfragment;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.budiyev.android.circularprogressbar.CircularProgressBar;
import com.codebosses.flicks.R;
import com.codebosses.flicks.activities.MoviesDetailActivity;
import com.codebosses.flicks.adapters.tvshowsadapter.TvShowsAdapter;
import com.codebosses.flicks.api.Api;
import com.codebosses.flicks.endpoints.EndpointKeys;
import com.codebosses.flicks.pojo.eventbus.EventBusTvShowDetailId;
import com.codebosses.flicks.pojo.eventbus.EventBusTvShowsClick;
import com.codebosses.flicks.pojo.tvpojo.TvMainObject;
import com.codebosses.flicks.pojo.tvpojo.TvResult;
import com.codebosses.flicks.utils.FontUtils;
import com.codebosses.flicks.utils.ValidUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class SuggestedTvShowsFragment extends Fragment {

    //    Android fields....
    @BindView(R.id.textViewErrorMessageSuggestedTvShows)
    TextView textViewError;
    @BindView(R.id.circularProgressBarSuggestedTvShows)
    CircularProgressBar circularProgressBar;
    @BindView(R.id.recyclerViewSuggestedTvShows)
    RecyclerView recyclerViewSuggestedTvShows;
    private LinearLayoutManager linearLayoutManager;

    //    Resource fields....
    @BindString(R.string.could_not_get_suggested_tv_shows)
    String couldNotGetMovies;
    @BindString(R.string.internet_problem)
    String internetProblem;

    //    Font fields....
    private FontUtils fontUtils;

    //    Retrofit fields....
    private Call<TvMainObject> suggestedTvShowsCall;

    //    Adapter fields....
    private List<TvResult> tvResultList = new ArrayList<>();
    private TvShowsAdapter tvShowsAdapter;
    private int pageNumber = 1, totalPages = 0;
    private String tvId = "";


    public SuggestedTvShowsFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggested_tv_shows, container, false);
        ButterKnife.bind(this, view);

        EventBus.getDefault().register(this);

        //        Setting custom font....
        fontUtils = FontUtils.getFontUtils(getActivity());
        fontUtils.setTextViewBoldFont(textViewError);

        if (getActivity() != null) {
            if (ValidUtils.isNetworkAvailable(getActivity())) {

                tvShowsAdapter = new TvShowsAdapter(getActivity(), tvResultList, EndpointKeys.SUGGESTED_TV_SHOWS);
                linearLayoutManager = new LinearLayoutManager(getActivity());
                recyclerViewSuggestedTvShows.setLayoutManager(linearLayoutManager);
                recyclerViewSuggestedTvShows.setItemAnimator(new DefaultItemAnimator());
                recyclerViewSuggestedTvShows.setAdapter(tvShowsAdapter);

            } else {
                textViewError.setVisibility(View.VISIBLE);
                textViewError.setText(internetProblem);
            }
        }
        recyclerViewSuggestedTvShows.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean isBottomReached = !recyclerView.canScrollVertically(1);
                if (isBottomReached) {
                    pageNumber++;
                    if (pageNumber <= totalPages)
                        getSuggestedTvShows(tvId, "en-US", pageNumber);
                }
            }
        });

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusGetTvDetail(EventBusTvShowDetailId eventBusTvShowDetailId) {
        circularProgressBar.setVisibility(View.VISIBLE);
        tvId = String.valueOf(eventBusTvShowDetailId.getTvId());
        getSuggestedTvShows(tvId, "en-US", pageNumber);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (suggestedTvShowsCall != null && suggestedTvShowsCall.isExecuted()) {
            suggestedTvShowsCall.cancel();
        }
        EventBus.getDefault().unregister(this);
    }

    private void getSuggestedTvShows(String tvId, String language, int pageNumber) {
        suggestedTvShowsCall = Api.WEB_SERVICE.getSuggestedTvShows(tvId, EndpointKeys.THE_MOVIE_DB_API_KEY, language, pageNumber);
        suggestedTvShowsCall.enqueue(new Callback<TvMainObject>() {
            @Override
            public void onResponse(Call<TvMainObject> call, retrofit2.Response<TvMainObject> response) {
                circularProgressBar.setVisibility(View.INVISIBLE);
                if (response != null && response.isSuccessful()) {
                    TvMainObject tvMainObject = response.body();
                    if (tvMainObject != null) {
                        totalPages = tvMainObject.getTotal_pages();
                        if (tvMainObject.getTotal_results() > 0) {
                            for (int i = 0; i < tvMainObject.getResults().size(); i++) {
                                tvResultList.add(tvMainObject.getResults().get(i));
                                tvShowsAdapter.notifyItemInserted(tvResultList.size() - 1);
                            }
                        }
                    }
                } else {
                    textViewError.setVisibility(View.VISIBLE);
                    textViewError.setText(couldNotGetMovies);
                }
            }

            @Override
            public void onFailure(Call<TvMainObject> call, Throwable error) {
                if (call.isCanceled() || "Canceled".equals(error.getMessage())) {
                    return;
                }
                circularProgressBar.setVisibility(View.INVISIBLE);
                textViewError.setVisibility(View.VISIBLE);
                if (error != null) {
                    if (error.getMessage().contains("No address associated with hostname")) {
                        textViewError.setText(internetProblem);
                    } else {
                        textViewError.setText(error.getMessage());
                    }
                } else {
                    textViewError.setText(couldNotGetMovies);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusSimilarTvClick(EventBusTvShowsClick eventBusTvShowsClick) {
        if (eventBusTvShowsClick.getTvShowType().equals(EndpointKeys.SUGGESTED_TV_SHOWS)) {
            Intent intent = new Intent(getActivity(), MoviesDetailActivity.class);
            intent.putExtra(EndpointKeys.MOVIE_ID, tvResultList.get(eventBusTvShowsClick.getPosition()).getId());
            intent.putExtra(EndpointKeys.MOVIE_TITLE, tvResultList.get(eventBusTvShowsClick.getPosition()).getName());
            intent.putExtra(EndpointKeys.RATING, tvResultList.get(eventBusTvShowsClick.getPosition()).getVote_average());
            startActivity(intent);
        }
    }

}