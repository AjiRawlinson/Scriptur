package com.example.scriptur;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scriptur.Database.DBAdaptor;
import com.example.scriptur.Database.Line;
import com.example.scriptur.Database.Scene;
import com.example.scriptur.RecyclerViewAdaptors.RVAdaptorScene;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SceneFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SceneFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SceneFragment extends Fragment implements RVAdaptorScene.OnRowListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RecyclerView rvScene;
    ArrayList<Scene> sceneList;
    DBAdaptor DBA;
    RVAdaptorScene RVAScene;
    TextView noData;
    int playid;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public SceneFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SceneFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SceneFragment newInstance(String param1, String param2) {
        SceneFragment fragment = new SceneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scene, container, false);

        DBA = new DBAdaptor(view.getContext());
        playid = getArguments().getInt("PLAY_ID");
        noData = (TextView) view.findViewById(R.id.tvSceneNoData);
        rvScene = (RecyclerView) view.findViewById(R.id.RVScene);
        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        rvScene.setLayoutManager(llm);
        sceneList = DBA.getAllScenesInPlay(playid);
        if(!sceneList.isEmpty()) {
            ArrayList<Line> lineList = new ArrayList<>();
            for(Scene scene: sceneList) {
                lineList.addAll(DBA.getAllLinesInScene(scene.getUID()));
            }
            RVAScene = new RVAdaptorScene(view.getContext(), sceneList, lineList, this);
            rvScene.setAdapter(RVAScene);
        } else { noData.setVisibility(View.VISIBLE); }

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String string) {
        if (mListener != null) {
            mListener.onSceneFragmentInteraction(string);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onSceneFragmentInteraction(String string);
    }

    @Override
    public void onRowClick(int position) {
        Intent in = new Intent(getActivity(), LineListActivity.class);
        in.putExtra("SCENE_ID", sceneList.get(position).getUID());
        startActivity(in);
    }

    @Override
    public void onRowLongClick(final int position, View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenu().add("Edit");
        popup.getMenu().add("Delete");
        popup.getMenu().add("Move Up");
        popup.getMenu().add("Move Down");
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.toString()) {
                    case "Edit":
                        Intent in = new Intent(getActivity(), UpdateSceneActivity.class);
                        in.putExtra("SCENE_ID", sceneList.get(position).getUID());
                        startActivity(in);
                        break;
                    case "Delete":
                        deleteSceneConfirmation(position);
                        break;
                    case "Move Up":
                        moveSceneUp(position);
                        break;
                    case "Move Down":
                        moveSceneDown(position);
                        break;
                    default://do nothing
                        break;
                }
                return true;
            }
        });
    }

    public void deleteSceneConfirmation(final int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        DBA.deleteScene(sceneList.get(position));
                        ArrayList<Scene> newScenesList = DBA.getAllScenesInPlay(playid);
                        for(int i = 0; i < newScenesList.size(); i++) {
                            sceneList.set(i, newScenesList.get(i));
                        }
                        sceneList.remove(sceneList.size() - 1);
                        RVAScene.notifyDataSetChanged();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
//                        do nothing
                        break;
                    default://do nothing
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete")
                .setMessage("Are you sure you want to delete Scene: " + sceneList.get(position).getName())
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    public void moveSceneUp(int position) {
        if(position > 0) {
            Scene sceneBelow = sceneList.get(position);
            Scene sceneAbove = sceneList.get(position - 1);

            DBA.changeSceneOrder(sceneAbove.getUID(), sceneBelow.getOrder());
            DBA.changeSceneOrder(sceneBelow.getUID(), sceneAbove.getOrder());
            updateRecyclerView();
        } else { Toast.makeText(getActivity(), "Can't move Scene up any further", Toast.LENGTH_LONG).show(); }
    }

    public void moveSceneDown(int position) {
        if(position < (sceneList.size() - 1)) {
            Scene sceneBelow = sceneList.get(position +1);
            Scene sceneAbove = sceneList.get(position);

            DBA.changeSceneOrder(sceneAbove.getUID(), sceneBelow.getOrder());
            DBA.changeSceneOrder(sceneBelow.getUID(), sceneAbove.getOrder());
            updateRecyclerView();
        } else { Toast.makeText(getActivity(), "Can't move Scene down any further", Toast.LENGTH_LONG).show(); }
    }

    public void updateRecyclerView() {
        ArrayList<Scene> newSceneList = DBA.getAllScenesInPlay(playid);
        for(int i = 0; i < newSceneList.size(); i++) {
            sceneList.set(i, newSceneList.get(i));
        }
        RVAScene.notifyDataSetChanged();
    }


}
