package com.example.farmlink
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.farmlink.databinding.ActivityMainBinding


//MainActivity tested II
class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment=HomeFragment()
        val fragmentManager=supportFragmentManager

        val fragmentTransaction=fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frameLa,fragment).commit()
    }
}
