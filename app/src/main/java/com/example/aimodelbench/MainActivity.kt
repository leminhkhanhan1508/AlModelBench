package com.example.aimodelbench


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.example.aimodelbench.composable.GridView
import com.example.aimodelbench.ml.SsdMobilenetV11Metadata1
import com.example.aimodelbench.model.AIModelEnum
import com.example.aimodelbench.model.FunctionItemUIModel
import com.example.aimodelbench.ui.theme.AIModelBenchTheme
import com.example.aimodelbench.utils.BatteryInfoHelper
import com.example.aimodelbench.utils.ModelBenchmarkEvaluator
import com.example.aimodelbench.utils.Utils
import org.tensorflow.lite.support.image.TensorImage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIModelBenchTheme {
                val gridItems = listOf(AIModelEnum.TEST, AIModelEnum.TEST1, AIModelEnum.TEST2, AIModelEnum.TEST3).map {
                    FunctionItemUIModel(
                        title = it.modelName,
                        enumCode = it
                    )
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GridView(
                        items = gridItems,
                        onItemClick = { code -> evaluateModelPerformance(code) },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

    }

    private fun evaluateModelPerformance(code: AIModelEnum) {
       when(code){
           AIModelEnum.TEST -> evaluateModel1()
           AIModelEnum.TEST1 -> logBatteryInfo(this)
           AIModelEnum.TEST2 -> actionGetRailInfo()
           AIModelEnum.TEST3 -> actionGetEnergyData()
       }
    }

    private fun evaluateModel1() {

        val evaluator = ModelBenchmarkEvaluator(
            modelName = "SsdMobilenetV11Metadata1",
            delegateUsed = "CPU",
            numThreads = 4
        )
        val model = SsdMobilenetV11Metadata1.newInstance(this)
        val dataSet = Utils.loadBitmapsFromFolder("Bộ nhớ trong/Download/SendAnywhere")
        val bitmap = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)?.let {
            val inputImage = TensorImage.fromBitmap(it.toBitmap())
            val file = File(this.filesDir, "benchmark_ssd_mobilenetv1.json")

            evaluator.evaluate(file) {
                model.process(inputImage)
                model.close()
            }
        }


    }

    companion object {
        init {
            System.loadLibrary("powerstats_jni")
        }
    }
    external fun getRailInfo(): String
    external fun getEnergyData(): String
    fun logBatteryInfo(context: Context){
        BatteryInfoHelper.getEnergyInfo(context)
    }

    fun actionGetRailInfo (){
        val result = getRailInfo()
        Log.wtf("KHANHANDEBUG", result)
    }

    fun actionGetEnergyData(){
        val result = getEnergyData()
        Log.wtf("KHANHANDEBUG", result)

    }
}
