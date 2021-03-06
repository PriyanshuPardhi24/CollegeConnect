package com.college.collegeconnect.adapters

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.college.collegeconnect.R
import com.college.collegeconnect.database.entity.SubjectDetails
import com.college.collegeconnect.datamodels.SaveSharedPreference
import com.college.collegeconnect.models.AttendanceViewModel
import com.github.lzyzsd.circleprogress.ArcProgress
import kotlin.collections.ArrayList

class SubjectAdapter(private val subjects: ArrayList<SubjectDetails>, private val context: Context, private val viewModel: AttendanceViewModel) : RecyclerView.Adapter<SubjectAdapter.ViewHolder>() {
    var per = 0
    var criteria = 0f
    var predict = 0f
    var attended: Int = 0
    var missed: Int = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subject_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        criteria = SaveSharedPreference.getAttendanceCriteria(context).toFloat()
        val current = subjects[position].subjectName
        holder.circleProgress.max = 100
        holder.circleProgress.progress = 25
        //Setting details of cards on loading
        holder.heading.text = current
        attended = subjects[position].attended
        missed = subjects[position].missed
        holder.ratio.text = "$attended/${missed + attended}"
        val percentage = String.format("%.0f", attended.toFloat() / (attended + missed) * 100)
        per = if (percentage == "NaN") 0 else percentage.toFloat().toInt()
        holder.circleProgress.progress = per
        predict = attended.toFloat() / (attended + missed + 1) * 100
        var i: Int
        if (predict < criteria && percentage != "NaN") {
            holder.tv_bunk.text = "You can\'t miss any more lectures"
        } else {
            if (percentage == "NaN") holder.tv_bunk.text = "No classes have happened yet" else {
                i = 1
                if (attended.toFloat() / (attended + missed + 2) * 100 >= criteria)
                    i = 2
                if (attended.toFloat() / (attended + missed + 3) * 100 >= criteria)
                    i = 3
                if (attended.toFloat() / (attended + missed + 4) * 100 >= criteria)
                    i = 4
                if (i == 4)
                    holder.tv_bunk.text = "You can miss more than 3 lectures"
                else holder.tv_bunk.text = "You can miss $i lecture(s)"
            }
        }
        holder.circleProgress.progress = per

        //Button functionality
        holder.increase.setOnClickListener {
            attended = subjects[position].attended
            missed = subjects[position].missed
            val sub = SubjectDetails(subjects[position].subjectName, attended + 1, missed)
            sub.id = subjects[position].id
            viewModel.updateSubject(sub)
            notifyDataSetChanged()
        }
        holder.decrease.setOnClickListener {
            attended = subjects[position].attended
            missed = subjects[position].missed
            val sub = SubjectDetails(subjects[position].subjectName, attended, missed + 1)
            sub.id = subjects[position].id
            viewModel.updateSubject(sub)
            notifyDataSetChanged()
        }
        holder.delete.setOnClickListener { view ->
            val popup = PopupMenu(context, view)
            val inflater = popup.menuInflater
            inflater.inflate(R.menu.actions, popup.menu)
            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.delete -> {
                        subjects[position].id.let { viewModel.delete(it) }
                        notifyDataSetChanged()
                    }
                    R.id.edit -> {

                        val builder = AlertDialog.Builder(context)

                        val inflater = (context as AppCompatActivity).layoutInflater
                        val view = inflater.inflate(R.layout.layout_edit_attendance, null)
                        val attended_edit = view.findViewById<EditText>(R.id.edit_attended)
                        val missed_edit = view.findViewById<EditText>(R.id.edit_missed)
                        val subject_edit = view.findViewById<EditText>(R.id.edit_attendance_title)
                        val total_classes = view.findViewById<EditText>(R.id.total_classes)
                        subject_edit.setText(current)
                        attended_edit.setText(attended.toString())
                        missed_edit.setText(missed.toString())
                        total_classes.setText((missed_edit.text.toString().toInt() + attended_edit.text.toString().toInt()).toString())
                        attended_edit.doAfterTextChanged {
                            if (it?.isNotEmpty()!! && missed_edit.text.toString().isNotEmpty())
                                total_classes.setText((missed_edit.text.toString().toInt() + attended_edit.text.toString().toInt()).toString())
                        }
                        missed_edit.doAfterTextChanged {
                            if (attended_edit.text.toString().isNotEmpty() && it?.isNotEmpty()!!)
                                total_classes.setText((missed_edit.text.toString().toInt() + attended_edit.text.toString().toInt()).toString())
                        }
                        builder.setView(view)
                                .setPositiveButton("Submit", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                                    val sub = SubjectDetails(subject_edit.text.toString(), attended_edit.text.toString().toInt(), missed_edit.text.toString().toInt())
                                    sub.id = subjects[position].id
                                    viewModel.updateSubject(sub)
                                    notifyDataSetChanged()
                                })
                                .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->

                                })
                        val dialog = builder.create()
                        dialog.show()
                    }
                }
                true
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int {
        return subjects.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var delete: ImageButton = itemView.findViewById(R.id.pop)
        var decrease: ImageView = itemView.findViewById(R.id.decrease)
        var increase: ImageView = itemView.findViewById(R.id.increase)
        var ratio: TextView = itemView.findViewById(R.id.qtyTextview)
        var heading: TextView = itemView.findViewById(R.id.subjectHeading)
        var tv_bunk: TextView = itemView.findViewById(R.id.tv_bunk)
        var circleProgress: ArcProgress = itemView.findViewById(R.id.arc_progress)

    }
}