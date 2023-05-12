package com.example.farmlink
//import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.farmlink.databinding.FragmentHomeBinding
import com.google.firebase.database.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

//home fragment of Farmer
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var db: DatabaseReference
    var nodeId = ""
    var sImage: String? = ""

    var testName = ""
    var itemArraylist = arrayListOf<String?>()


    // pass data between fragments ,activities
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nodeId = it.getString("itm_id").toString()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment class that can be used to inflate a layout file into a view
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.button3.setOnClickListener() {
            var myfileintent = Intent(Intent.ACTION_GET_CONTENT)
            myfileintent.setType("image/*")

            ActivityResultLauncher.launch(myfileintent)
        }
        //calling insert data fun
        binding.button4.setOnClickListener() {
            add_Data()
        }
        //calling generate_pdf function
        binding.buttonf.setOnClickListener() {
            generate_pdf()
        }

        //trasaction to item page frament
        binding.button5.setOnClickListener() {
            val fragment = ListFragment()
            val fragmentManager = activity?.supportFragmentManager
            val fragmentTrasaction = fragmentManager!!.beginTransaction()
            fragmentTrasaction.replace(R.id.frameLa, fragment)
                .addToBackStack(HomeFragment().toString()).commit()

        }
        if (nodeId != "") {
            disply_data()
        }
        //update
        binding.button8.setOnClickListener() {
            update_data()
        }
        //delete function calling
        binding.button9.setOnClickListener() {
            delete_data()
        }
        return root
    }


    //delete function
    private fun delete_data() {
        db = FirebaseDatabase.getInstance().getReference("items")
        db.child(nodeId).removeValue().addOnSuccessListener {
            binding.etName.text.clear()
            binding.etPrice.text.clear()
            binding.etDes.text.clear()
            sImage = ""
            binding.imageView.setImageBitmap(null)

            binding.button8.visibility = View.INVISIBLE
            binding.button9.visibility = View.INVISIBLE
            binding.button4.visibility = View.VISIBLE

            Toast.makeText(context, "Food Deleted", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener() {
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    //update function
    private fun update_data() {
        val itemName = binding.etName.text.toString()
        val itemPrice = binding.etPrice.text.toString()
        val description = binding.etDes.text.toString()
        val dtclass = Dtclass()

        db = FirebaseDatabase.getInstance().getReference("items")
        val item = itemDs(itemName, itemPrice, sImage, description)

        db.child(nodeId).setValue(item).addOnSuccessListener {
            binding.etName.text.clear()
            binding.etPrice.text.clear()
            binding.etDes.text.clear()
            binding.imageView.setImageBitmap(null)
            sImage = ""
            binding.button8.visibility = View.INVISIBLE
            binding.button9.visibility = View.INVISIBLE
            binding.button4.visibility = View.VISIBLE
            binding.buttonf.visibility = View.VISIBLE
            Toast.makeText(context, "Food Updated", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {
            Toast.makeText(context, "not inserted", Toast.LENGTH_SHORT).show()

        }


    }

    //display data
    private fun disply_data() {
        db = FirebaseDatabase.getInstance().getReference("items")
        db.child(nodeId).get().addOnSuccessListener {

            if (it.exists()) {
                testName = it.child("itemName").value.toString()

                val dtclass = Dtclass()
                val itemDs = itemDs()
                binding.etName.setText(it.child("itemName").value.toString())
                binding.etPrice.setText(it.child("itemPrice").value.toString())
                binding.etDes.setText(it.child("description").value.toString())

                sImage = it.child("itemImg").value.toString()
                val bytes = Base64.decode(sImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                binding.imageView.setImageBitmap(bitmap)
                binding.button8.visibility = View.VISIBLE
                binding.button9.visibility = View.VISIBLE
                binding.button4.visibility = View.INVISIBLE
                binding.buttonf.visibility = View.INVISIBLE

            }
        }

    }

    //genarate pdf
    private fun generate_pdf() {
        //get db instance
        db = FirebaseDatabase.getInstance().getReference("items")
        var query: Query
        query = db.orderByChild("itemName")
        query.addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemArray = arrayListOf<String?>()
                if (snapshot.exists()) {
                    for (itmsnapshot in snapshot.children) {
                        //create obj from itemDs
                        val item = itmsnapshot.getValue(itemDs::class.java)
                        //add if its not null
                        if (item != null) {
                            itemArray.add(item.itemName)
                            itemArray.add(item.itemPrice)
                        }

                    }

                    val document = Document()
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                    val fileName = current.format(formatter).toString()
                    val filePath =
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                            .toString() + "/" + fileName + ".pdf"
                    val writer = PdfWriter.getInstance(document, FileOutputStream(filePath))

                    document.open()

                    val font = FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD, 14f,
                        BaseColor.BLACK
                    )
                    //pdf heading
                    val heading = Paragraph("FarmLink Price List", font)
                    heading.alignment = Element.ALIGN_CENTER
                    document.add(heading)

                    document.add(Chunk.NEWLINE)
                    //table create
                    val table = PdfPTable(2)
                    val header = PdfPCell()
                    val headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD)
                    header.backgroundColor = BaseColor.LIGHT_GRAY
                    header.horizontalAlignment = Element.ALIGN_CENTER
                    header.borderWidth = 2f
                    header.phrase = Phrase("Item", headFont)
                    table.addCell(header)
                    header.phrase = Phrase("Price", headFont)
                    table.addCell(header)

                    for (aw in 0..itemArray.size - 1) {
                        table.addCell(itemArray[aw])
                    }
                    document.add(table)
                    document.close()
                    writer.close()

                    Toast.makeText(context, "PDF generated", Toast.LENGTH_SHORT).show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }


    //insert data fun
    private fun add_Data() {
        val itemName = binding.etName.text.toString()
        if (itemName.isNullOrEmpty()) {
            // Show an error message or toast to indicate that the itemName is required
            Toast.makeText(context, "Please enter an item name", Toast.LENGTH_SHORT).show()
            return
        }
        val itemPrice = binding.etPrice.text.toString()

        val description = binding.etDes.text.toString()
        if (description.isNullOrEmpty()) {
            // Show an error message or toast to indicate that the itemName is required
            Toast.makeText(context, "Please enter Decription", Toast.LENGTH_SHORT).show()
            return
        }
        db = FirebaseDatabase.getInstance().getReference("items")
        val item = itemDs(itemName, itemPrice, sImage, description)
        val databaseReference = FirebaseDatabase.getInstance().reference
        val id = databaseReference.push().key
        db.child(id.toString()).setValue(item).addOnSuccessListener {
            binding.etName.text.clear()
            binding.etPrice.text.clear()
            sImage = ""
            binding.etDes.text.clear()
            binding.imageView.setImageBitmap(null)
            Toast.makeText(context, "Data Inserted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(context, it.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    //handle the result of starting an activity
    private val ActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val uri = result.data!!.data
            try {
                //compressed into a PNG format and converted to a ByteArray
                val inputStream = context?.contentResolver?.openInputStream(uri!!)
                val myBitmap = BitmapFactory.decodeStream(inputStream)
                val stream = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val bytes = stream.toByteArray()
                sImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                binding.imageView.setImageBitmap(myBitmap)
                inputStream!!.close()
                Toast.makeText(context, "Image Selected", Toast.LENGTH_SHORT).show()
            } catch (ex: Exception) {
                Toast.makeText(context, ex.message.toString(), Toast.LENGTH_LONG).show()
            }
        }

    }

}