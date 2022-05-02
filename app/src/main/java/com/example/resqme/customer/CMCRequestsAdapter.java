package com.example.resqme.customer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.resqme.R;
import com.example.resqme.model.CMCRequest;
import com.example.resqme.model.NotificationResQme;
import com.example.resqme.model.Rate;
import com.example.resqme.model.RequestDetailsModel;
import com.example.resqme.model.ServiceProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CMCRequestsAdapter extends RecyclerView.Adapter<CMCRequestsAdapter.CMCRequestsAdapterViewHolder> {
    Context context, context_2;
    ArrayList<CMCRequest> cmcRequests;
    DatabaseReference referenceSP;
    View view;
    FirebaseAuth firebaseAuth;
    public CMCRequestsAdapter(Context context, ArrayList<CMCRequest> cmcRequests, DatabaseReference referenceSP
    ,Context context_2, View view) {
        this.context = context;
        this.cmcRequests = cmcRequests;
        this.referenceSP = referenceSP;
        this.context_2 = context_2;
        this.view = view;
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public CMCRequestsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cmc_requests_item, parent, false);
        return new CMCRequestsAdapter.CMCRequestsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CMCRequestsAdapterViewHolder holder, @SuppressLint("RecyclerView") int position) {

        // Whatever status
        holder.tvCMCRequestTimestamp.setText(cmcRequests.get(position).getRequestCMCTimestamp());
        holder.tvCMCRequestDescription.setText(cmcRequests.get(position).getCmcRequestDescription());

        // Status depending
        if(cmcRequests.get(position).getCmcRequestStatus().equals("Pending")){
            holder.tvCMCRequestStatus.setText("قيد المراجعة");
            holder.tvCMCRequestStatus.setTextColor(Color.rgb(255, 166, 53));
            holder.CompleteBtn.setEnabled(false);
            holder.CancelBtn.setEnabled(false);
            holder.tvCMCRequestOwnerName.setText("غير متاح حتى قبول الطلب");
            holder.tvCMCRequestOwnerName.setTextColor(Color.rgb(255, 166, 53));
            holder.tvCMCRequestOwnerPhone.setText("غير متاح حتى قبول الطلب");
            holder.tvCMCRequestOwnerPhone.setTextColor(Color.rgb(255, 166, 53));
        }else if(cmcRequests.get(position).getCmcRequestStatus().equals("Approved")){
            holder.tvCMCRequestStatus.setText("تم قبول الطلب.");
            holder.tvCMCRequestStatus.setTextColor(Color.GREEN);
            holder.CompleteBtn.setEnabled(true);
            holder.CancelBtn.setEnabled(true);

            //Sending notification
            DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
            NotificationResQme notification = new NotificationResQme(cmcRequests.get(position).getCmcRequestID(), "إشعار بخصوص طلب مركز صيانة", "تم قبول طلبك من صاحب مركز الصيانة، لمعرفة المزيد من فضلك اذهب الى صفحة طلبات مراكز الصيانة.", FirebaseAuth.getInstance().getCurrentUser().getUid());
            notificationRef.child(cmcRequests.get(position).getCmcRequestID()).setValue(notification);

            //Getting winch name, owner name, owner phone using firebase
            //hide owner name and phone until approval
            referenceSP.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ServiceProvider serviceProvider = dataSnapshot.getValue(ServiceProvider.class);
                        if(serviceProvider.getUserId().equals(cmcRequests.get(position).getCmcOwnerID())){
                            holder.tvCMCRequestOwnerName.setText(serviceProvider.getUsername());
                            holder.tvCMCRequestOwnerPhone.setText(serviceProvider.getWhatsApp());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }else if(cmcRequests.get(position).getCmcRequestStatus().equals("Refused")){
            holder.tvCMCRequestStatus.setText("تم رفض الطلب");
            holder.tvCMCRequestStatus.setTextColor(Color.RED);
            holder.CompleteBtn.setEnabled(false);
            holder.CancelBtn.setEnabled(false);
            holder.tvCMCRequestOwnerName.setText("غير متاح");
            holder.tvCMCRequestOwnerName.setTextColor(Color.RED);
            holder.tvCMCRequestOwnerPhone.setText("غير متاح");
            holder.tvCMCRequestOwnerPhone.setTextColor(Color.RED);

            //Sending notification
            DatabaseReference notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
            NotificationResQme notification = new NotificationResQme(cmcRequests.get(position).getCmcRequestID(), "إشعار بخصوص طلب مركز صيانة", "للأسف تم رفض طلبك من صاحب مركز الصيانة، يمكنك الآن تقييم الخدمة.", FirebaseAuth.getInstance().getCurrentUser().getUid());
            notificationRef.child(cmcRequests.get(position).getCmcRequestID()).setValue(notification);

        }

        if(cmcRequests.get(position).getCmcRequestStatus().equals("Success")){
            holder.tvCMCRequestStatus.setText("تم الطلب بنجاح");
            holder.tvCMCRequestStatus.setTextColor(Color.BLUE);
            holder.rateBtn.setEnabled(true);
            holder.CompleteBtn.setEnabled(false);
            holder.CancelBtn.setEnabled(false);

            referenceSP.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        ServiceProvider serviceProvider = dataSnapshot.getValue(ServiceProvider.class);
                        if(serviceProvider.getUserId().equals(cmcRequests.get(position).getCmcOwnerID())){
                            holder.tvCMCRequestOwnerName.setText(serviceProvider.getUsername());
                            holder.tvCMCRequestOwnerPhone.setText(serviceProvider.getWhatsApp());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        if(cmcRequests.get(position).getCmcRequestStatus().equals("Failed")){
            holder.tvCMCRequestStatus.setText("تم إلغاء الطلب");
            holder.tvCMCRequestStatus.setTextColor(Color.RED);
            holder.rateBtn.setEnabled(false);
            holder.CompleteBtn.setEnabled(false);
            holder.CancelBtn.setEnabled(false);
            holder.tvCMCRequestOwnerName.setText("غير متاح");
            holder.tvCMCRequestOwnerName.setTextColor(Color.RED);
            holder.tvCMCRequestOwnerPhone.setText("غير متاح");
            holder.tvCMCRequestOwnerPhone.setTextColor(Color.RED);
        }


        holder.CompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("CMCRequests");
                requestRef.child(cmcRequests.get(position).getCmcRequestID()).child("cmcRequestStatus").setValue("Success");
                Toast.makeText(context, "لقد قمت بإنهاء الطلب بنجاح، يمكنك تقييم الخدمة الآن.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.rateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRateDialog(cmcRequests.get(position).getCmcRequestID(), cmcRequests.get(position).getCmcOwnerID(),
                        cmcRequests.get(position).getCustomerID());
            }
        });

        holder.CancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Failed
                DatabaseReference requestRef = FirebaseDatabase.getInstance().getReference().child("CMCRequests");
                requestRef.child(cmcRequests.get(position).getCmcRequestID()).child("cmcRequestStatus").setValue("Failed");
                Toast.makeText(context, "لقد قمت بإنهاء الطلب بشكل مفاجئ.", Toast.LENGTH_SHORT).show();
                holder.rateBtn.setEnabled(false);
                holder.CompleteBtn.setEnabled(false);
                holder.CancelBtn.setEnabled(false);
            }
        });


         DatabaseReference rate = FirebaseDatabase.getInstance().getReference().child("Rate");
        rate.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Rate rate = dataSnapshot.getValue(Rate.class);
                    if(rate.getRequestID().equals(cmcRequests.get(position).getCmcRequestID())
                            && rate.getCustomerID().equals(firebaseAuth.getCurrentUser().getUid())){
                        holder.rateBtn.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    @Override
    public int getItemCount() {
        return cmcRequests.size();
    }

    private void openRateDialog(String cmcRequestID, String cmcOwnerID, String customerID) {
        BottomSheetDialog rateDialog;
        rateDialog = new BottomSheetDialog(context_2, R.style.BottomSheetDialogTheme);

        View rateBottomView = LayoutInflater.from(context_2).inflate(R.layout.rate_bottom_layout,
                (LinearLayout) view.findViewById(R.id.bottom_sheet_rate_linear_layout));

        RatingBar ratingBar = rateBottomView.findViewById(R.id.rating_bar_page);
        TextInputEditText rateText = rateBottomView.findViewById(R.id.rating_description_et);
        MaterialButton saveRateBtn = rateBottomView.findViewById(R.id.save_rating_btn);


        rateDialog.setContentView(rateBottomView);
        rateDialog.show();


        saveRateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(rateText.getText().toString().trim())){
                    if(String.valueOf(ratingBar.getRating()).equals("0.0")){
                        Toast.makeText(context_2, "من فضلك اختر تقييم من 1 الى 5", Toast.LENGTH_SHORT).show();
                    }else{
                        // We are service provider
                        ProgressDialog progressDialog = new ProgressDialog(context_2);
                        progressDialog.setMessage("انتظر قليلاً...");
                        progressDialog.show();

                        Query query = FirebaseDatabase.getInstance().getReference("ServiceProviders").
                                orderByChild("userId").equalTo(cmcOwnerID);
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                                    ServiceProvider serviceProvider = dataSnapshot.getValue(ServiceProvider.class);
                                    double totalNewRate = (Double.parseDouble(serviceProvider.getRate()) + Double.parseDouble(String.valueOf(ratingBar.getRating()))) / 2;
                                    DatabaseReference spTable = FirebaseDatabase.getInstance().getReference().child("ServiceProviders");
                                    spTable.child(cmcOwnerID).child("rate").setValue(String.valueOf(totalNewRate));
                                    // Save the rate in the rate table
                                    DatabaseReference rateTable = FirebaseDatabase.getInstance().getReference().child("Rate");
                                    String rateID = rateTable.push().getKey();
                                    Rate rate = new Rate(rateID, customerID, cmcOwnerID, String.valueOf(ratingBar.getRating()), rateText.getText().toString().trim(), cmcRequestID, "Customer");
                                    rateTable.child(rateID).setValue(rate);

                                    progressDialog.dismiss();
                                    Toast.makeText(context_2, "تمت عملية التقييم بنجاح!", Toast.LENGTH_SHORT).show();
                                    rateDialog.cancel();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }else{
                    Toast.makeText(context, "من فضلك قم بكتابة تقييم...", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    public class CMCRequestsAdapterViewHolder extends RecyclerView.ViewHolder{
        TextView tvCMCRequestStatus, tvCMCRequestName, tvCMCRequestOwnerName, tvCMCRequestOwnerPhone,
                tvCMCRequestDescription, tvCMCRequestTimestamp;
        MaterialButton CompleteBtn, CancelBtn, rateBtn;
        public CMCRequestsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCMCRequestStatus = itemView.findViewById(R.id.cmc_request_item_status_txt);
            tvCMCRequestName = itemView.findViewById(R.id.cmc_request_item_cmc_name_txt);
            tvCMCRequestOwnerName = itemView.findViewById(R.id.cmc_request_item_cmc_owner_name_txt);
            tvCMCRequestOwnerPhone = itemView.findViewById(R.id.cmc_request_item_cmc_owner_phone_txt);
            tvCMCRequestDescription = itemView.findViewById(R.id.cmc_request_description_txt);
            tvCMCRequestTimestamp = itemView.findViewById(R.id.cmc_request_timestamp_txt);
            CompleteBtn = itemView.findViewById(R.id.cmc_request_complete_btn);
            CancelBtn = itemView.findViewById(R.id.cmc_request_cancel_btn);
            rateBtn = itemView.findViewById(R.id.cmc_request_rate_from_customer_btn);
        }
    }
}
