package com.intprog.eventmanager_gitbam.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intprog.eventmanager_gitbam.R
import com.intprog.eventmanager_gitbam.app.EventManagerApplication
import com.intprog.eventmanager_gitbam.models.Event
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class CreateEventFragment : Fragment() {
    private lateinit var requestQueue: RequestQueue
    private val TAG = "CreateEventFragment"

    // Form fields
    private lateinit var etName: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etAddress: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etImageUrl: TextInputEditText
    private lateinit var etDetailImageUrl: TextInputEditText
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var spinnerPriceType: AutoCompleteTextView
    private lateinit var etPrice: TextInputEditText
    private lateinit var btnSubmit: Button
    private lateinit var btnCancel: Button
    private lateinit var progressBar: ProgressBar

    // Categories
    private val categories = arrayOf(
        "Conference", "Workshop", "Seminar", "Exhibition",
        "Concert", "Sports", "Networking", "Other"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize RequestQueue
        requestQueue = Volley.newRequestQueue(requireContext())

        // Initialize views
        initializeViews(view)
        setupSpinners()
        setupClickListeners()
    }

    private fun initializeViews(view: View) {
        // Text fields
        etName = view.findViewById(R.id.et_event_name)
        etDate = view.findViewById(R.id.et_event_date)
        etLocation = view.findViewById(R.id.et_event_location)
        etAddress = view.findViewById(R.id.et_event_address)
        etDescription = view.findViewById(R.id.et_event_description)
        etImageUrl = view.findViewById(R.id.et_event_image)
        etDetailImageUrl = view.findViewById(R.id.et_event_detail_image)
        etPrice = view.findViewById(R.id.et_event_price)

        // Spinners
        spinnerCategory = view.findViewById(R.id.spinner_category)
        spinnerPriceType = view.findViewById(R.id.spinner_price_type)

        // Buttons
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnCancel = view.findViewById(R.id.btn_cancel)
        progressBar = view.findViewById(R.id.progress_bar)

        // Set minimum date for date picker
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        etDate.setText(dateFormat.format(calendar.time))
    }

    private fun setupSpinners() {
        // Category spinner
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        spinnerCategory.setAdapter(categoryAdapter)

        // Price type spinner
        val priceTypes = arrayOf("Free", "Paid")
        val priceTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            priceTypes
        )
        spinnerPriceType.setAdapter(priceTypeAdapter)

        // Show/hide price field based on price type
        spinnerPriceType.setOnItemClickListener { _, _, position, _ ->
            etPrice.visibility = if (position == 1) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            if (validateForm()) {
                createEvent()
            }
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Date picker
        etDate.setOnClickListener {
            showDatePicker()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Required fields validation
        if (etName.text.toString().trim().isEmpty()) {
            etName.error = "Event name is required"
            isValid = false
        }

        if (etDate.text.toString().trim().isEmpty()) {
            etDate.error = "Event date is required"
            isValid = false
        }

        if (etLocation.text.toString().trim().isEmpty()) {
            etLocation.error = "Location is required"
            isValid = false
        }

        if (etAddress.text.toString().trim().isEmpty()) {
            etAddress.error = "Address is required"
            isValid = false
        }

        if (etDescription.text.toString().trim().isEmpty()) {
            etDescription.error = "Description is required"
            isValid = false
        }

        // Price validation for paid events
        if (spinnerPriceType.text.toString() == "Paid") {
            val price = etPrice.text.toString().trim()
            if (price.isEmpty() || price.toDoubleOrNull() == null || price.toDouble() <= 0) {
                etPrice.error = "Valid price is required"
                isValid = false
            }
        }

        return isValid
    }

    private fun createEvent() {
        showLoading(true)

        val username = (requireActivity().application as EventManagerApplication).username
        val url = "https://sysarch.glitch.me/api/events"

        val eventData = JSONObject().apply {
            put("name", etName.text.toString().trim())
            put("date", etDate.text.toString().trim())
            put("location", etLocation.text.toString().trim())
            put("address", etAddress.text.toString().trim())
            put("description", etDescription.text.toString().trim())
            put("category", spinnerCategory.text.toString())
            put("price", if (spinnerPriceType.text.toString() == "Paid") 
                etPrice.text.toString().toDouble() else 0.0)
            put("image", etImageUrl.text.toString().trim())
            put("detailImage", etDetailImageUrl.text.toString().trim())
            put("organizer", username)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            eventData,
            { response ->
                showLoading(false)
                if (response.getString("message") == "Event created successfully") {
                    Toast.makeText(requireContext(), "Event created successfully", Toast.LENGTH_LONG).show()
                    parentFragmentManager.popBackStack()
                } else {
                    showError("Failed to create event: ${response.getString("message")}")
                }
            },
            { error ->
                showLoading(false)
                showError("Failed to create event: ${error.message}")
            }
        )

        requestQueue.add(request)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etDate.setText(dateFormat.format(date.time))
            },
            year,
            month,
            day
        ).show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !show
        btnCancel.isEnabled = !show
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        requestQueue.cancelAll(TAG)
    }
} 