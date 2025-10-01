package com.example.aimodelbench


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.aimodelbench.ml.MobileNetV2
import com.example.aimodelbench.ml.SsdMobilenetV11Metadata1
import com.example.aimodelbench.utils.ModelBenchmarkEvaluator
import com.example.aimodelbench.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var progress by remember { mutableStateOf(0) }
            var isFinished by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isFinished) {
                    LinearProgressIndicator(
                        progress = progress / 100f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Đang xử lý: $progress%")
                } else {
                    Text(text = "Hoàn tất ✅")
                }

            }

            // Chạy evaluateModel1 khi UI khởi tạo
            LaunchedEffect(Unit) {
                evaluateModel { percent ->
                    progress = percent
                    if (percent == 100) {
                        isFinished = true
                    }
                }
            }
        }

    }

    private fun evaluateModel(onProgressUpdate: (Int) -> Unit) {
        lifecycleScope.launch(Dispatchers.Default){
            val evaluator = ModelBenchmarkEvaluator(
                modelName = "SsdMobilenetV11Metadata1",
                delegateUsed = "CPU",
                numThreads = 4,
                Utils.getTotalRam(this@MainActivity)
            )
            val model = MobileNetV2.newInstance(this@MainActivity)
            val dataSet = Utils.loadBitmapsFromAssets(this@MainActivity, "dataset")
            val file = File(this@MainActivity.filesDir, "benchmark_ssd_mobilenetv1.json")
            evaluator.evaluate(file) {
                repeat(10) {
                    dataSet.forEachIndexed { index, item ->
                        val progress = (it + 1) * 100 / 10
                        onProgressUpdate.invoke(progress)
//                        val inputImage = TensorImage.fromBitmap(item)
                        val inputImage = Utils.bitmapToTensorBuffer(
                            item
                        )
                        model.process(inputImage)
                    }
                }
                model.close()
            }
        }

    }
}
