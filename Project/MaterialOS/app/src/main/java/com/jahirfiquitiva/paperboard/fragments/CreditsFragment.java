package com.jahirfiquitiva.paperboard.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jahirfiquitiva.paperboard.util.Util;

import org.materialos.icons.R;

import java.util.ArrayList;
import java.util.List;

public class CreditsFragment extends Fragment {

    private List<Item> mItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_cards_recycler, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(R.string.about);


        mItems = new ArrayList<>();


        for (int i = 1; ; i++) {
            int id = getCreditsStringResource("" + i);
            if (id == 0) {
                break;
            }
            Item item = new Item();
            item.name = getString(id);

            int subtitle = getCreditsStringResource(i + "_subtitle");
            if (subtitle != 0) {
                //Not a subheader
                item.subtitle = getString(subtitle);
                item.desc = getString(getCreditsStringResource(i + "_desc"));

                int website = getCreditsStringResource(i + "_website");
                if (website != 0) {
                    item.website = getString(website);
                }

                int gplus = getCreditsStringResource(i + "_gplus");
                if (gplus != 0) {
                    item.gplus = getString(gplus);
                }
            } else {
                item.header = true;
            }

            mItems.add(item);
        }

        recyclerView.setAdapter(new CreditsRecyclerAdapter());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        return recyclerView;
    }

    private int getCreditsStringResource(String i) {
        return getResources().getIdentifier("credits_item" + i, "string", getActivity().getPackageName());
    }

    private class Item {
        boolean header;

        String name;
        String subtitle;
        String desc;

        String website;
        String gplus;
    }

    private class CreditsRecyclerAdapter extends RecyclerView.Adapter<CreditsRecyclerAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_credits, parent, false);
                return new ViewHolder(view, false);
            } else {
                View view = getActivity().getLayoutInflater().inflate(R.layout.list_item_credits_subheader, parent, false);
                return new ViewHolder(view, true);
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Item item = mItems.get(position);

            if (item.header) {
                holder.name.setText(item.name);
                return;
            }

            holder.name.setText(item.name);
            holder.subtitle.setText(item.subtitle);
            holder.desc.setText(item.desc);


            if (item.gplus == null && item.website == null) {
                holder.actionButtonBar.setVisibility(View.GONE);
                holder.desc.setPadding(0, 0, 0, Util.convertToPixel(getActivity(), 24));
            } else {
                holder.actionButtonBar.setVisibility(View.VISIBLE);
                holder.desc.setPadding(0, 0, 0, Util.convertToPixel(getActivity(), 16));

                if (item.website != null) {
                    holder.website.setVisibility(View.VISIBLE);
                    holder.website.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(item.website));
                            startActivity(intent);
                        }
                    });
                } else {
                    holder.website.setVisibility(View.GONE);
                }

                if (item.gplus != null) {
                    holder.gplus.setVisibility(View.VISIBLE);
                    holder.gplus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO,
                                    Uri.fromParts(
                                            "mailto", item.gplus, null));
                            startActivity(intent);
                        }
                    });
                } else {
                    holder.gplus.setVisibility(View.GONE);
                }
            }

        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).header ? 0 : 1;
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView name;
            private TextView subtitle;
            private TextView desc;
            private Button website;
            private Button gplus;
            private LinearLayout actionButtonBar;

            public ViewHolder(View itemView, boolean header) {
                super(itemView);
                if (header) {
                    name = (TextView) itemView;
                    return;
                }

                name = (TextView) itemView.findViewById(R.id.list_item_credits_title);


                subtitle = (TextView) itemView.findViewById(R.id.list_item_credits_subtitle);
                desc = (TextView) itemView.findViewById(R.id.list_item_credits_desc);
                website = (Button) itemView.findViewById(R.id.list_item_credits_website_button);
                gplus = (Button) itemView.findViewById(R.id.list_item_credits_gplus_button);
                actionButtonBar = (LinearLayout) itemView.findViewById(R.id.list_item_credits_actions);

                View.OnClickListener l = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String website = mItems.get(getLayoutPosition()).website;
                        if (website != null) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                            startActivity(i);
                        }
                    }
                };
                itemView.setOnClickListener(l);
                website.setOnClickListener(l);
                gplus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String gplus = mItems.get(getLayoutPosition()).gplus;
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(gplus));
                        startActivity(i);
                    }
                });
            }
        }
    }

}
