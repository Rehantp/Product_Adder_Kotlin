package com.example.farmlink

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.farmlink.databinding.FragmentListBinding
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseError
import org.w3c.dom.NodeList

// properties and functions to handle the logic and display of a list of items.
class ListFragment : Fragment() {
    private lateinit var db: DatabaseReference
    private lateinit var itemArraylist: ArrayList<itemDs>
    private lateinit var nodeList: ArrayList<tempData>
    private lateinit var binding: FragmentListBinding

    private var d1: Long = 0
    private var d2: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    // creating and returning the view hierarchy associated with the fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false)
        binding.itemList.layoutManager = LinearLayoutManager(context)
        binding.itemList.hasFixedSize()

        itemArraylist = arrayListOf<itemDs>()
        nodeList = arrayListOf<tempData>()
        getItemData()
        return binding.root


    }

    //that retrieves data from the Firebase Realtime Database and populates the itemArraylist and nodeList
    private fun getItemData() {
        db = FirebaseDatabase.getInstance().getReference("items")
        var query: Query
        query = db.orderByChild("itemName")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    var ky: String = ""
                    var itnm: String = ""
                    for (itmsnapshot in snapshot.children) {

                        val item = itmsnapshot.getValue(itemDs::class.java)
                        itemArraylist.add(item!!)
                        ky = itmsnapshot.key.toString()
                        itnm = item.itemName.toString()
                        val tmpitm = tempData(ky, itnm)
                        nodeList.add(tmpitm)


                    }
                    var adapter = itmAdapter(itemArraylist)
                    binding.itemList.adapter = adapter
                    adapter.setOnItemClickListner(object : itmAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val ctitm = nodeList[position]
                            val nodePath = ctitm.id.toString()
                            val fragment = HomeFragment()
                            val bundle = Bundle()
                            bundle.putString("itm_id", nodePath.toString())
                            fragment.arguments = bundle
                            val fragmentManager = activity?.supportFragmentManager
                            val fragmentTransaction = fragmentManager!!.beginTransaction()
                            fragmentTransaction.replace(
                                com.example.farmlink.R.id.frameLayout2,
                                fragment
                            ).commit()
                        }

                    })

                }
            }


            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }


}