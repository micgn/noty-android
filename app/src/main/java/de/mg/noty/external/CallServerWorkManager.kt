package de.mg.noty.external

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.work.*
import com.fasterxml.jackson.databind.ObjectMapper
import de.mg.noty.db.NotyDatabase
import de.mg.noty.external.dto.req.AllContentDto
import de.mg.noty.external.dto.req.NoteDeltaDto
import de.mg.noty.external.dto.req.NoteTagDeltaDto
import de.mg.noty.external.dto.req.TagDeltaDto
import de.mg.noty.model.MetaData
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class CallServerWorkManager {

    private val uniqueWorkName = "sequence";
    private val mapper = ObjectMapper()

    fun create(dto: NoteDeltaDto) {
        enqueueServerCall("POST", "note", dto)
    }

    fun update(dto: NoteDeltaDto) {
        enqueueServerCall("PUT", "note", dto)
    }

    fun delete(dto: NoteDeltaDto) {
        enqueueServerCall("DELETE", "note", dto)
    }

    fun create(dto: TagDeltaDto) {
        enqueueServerCall("POST", "tag", dto)
    }

    fun update(dto: TagDeltaDto) {
        enqueueServerCall("PUT", "tag", dto)
    }

    fun delete(dto: TagDeltaDto) {
        enqueueServerCall("DELETE", "tag", dto)
    }

    fun create(dto: NoteTagDeltaDto) {
        enqueueServerCall("POST", "notetag", dto)
    }

    fun delete(dto: NoteTagDeltaDto) {
        enqueueServerCall("DELETE", "notetag", dto)
    }

    fun getDeltas() {
        enqueueServerCall("GET", "deltas")
    }

    fun overwriteAll(dto: AllContentDto) {
        // directly call the server without WorkManager

        Background {

            try {
                val jsonPayload = mapper.writeValueAsString(dto)
                Log.d("CallServerWorkManager", "overwrite: sending ${jsonPayload.length} data")
                HttpClientService.send("POST", "all", jsonPayload, 0)
            } catch (e: Exception) {
                Log.e("CallServerWorkManager", "error while sending", e)
            }
        }.execute()
    }

    private class Background(val callback: () -> Unit) : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {
            callback()
            return null
        }
    }

    fun getQueueInfo(): String? {
        var result: String? = null
        runBlocking {
            val workInfos =
                WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueWorkName).await()
            result = workInfos.groupBy { it.state }.mapValues { it.value.size }
                .map { "${it.key}:${it.value}" }.joinToString(", ")
        }
        return result
    }

    fun hasFailures(): Boolean {
        var result: Boolean = false
        runBlocking {
            val workInfos =
                WorkManager.getInstance().getWorkInfosForUniqueWork(uniqueWorkName).await()
            result = workInfos.filter { it.state == WorkInfo.State.FAILED }.any()
        }
        return result
    }

    fun cancelAll() {
        WorkManager.getInstance().cancelAllWork()
    }

    private fun enqueueServerCall(
        httpMethod: String,
        path: String,
        requestEntity: Any? = null
    ) {

        val jsonPayload =
            if (requestEntity != null) mapper.writeValueAsString(requestEntity) else null
        Log.d("CallServerWorkManager", "sending ${jsonPayload?.length} data")
        val data = workDataOf(
            "method" to httpMethod,
            "path" to path, "json" to jsonPayload
        )

        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val request = OneTimeWorkRequestBuilder<CallTheServerWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setInputData(data)
            .build()

        // using a unique work sequence to prevent execution in parallel
        WorkManager.getInstance()
            .beginUniqueWork(uniqueWorkName, ExistingWorkPolicy.APPEND, request).enqueue()
    }


    class CallTheServerWorker(private val appContext: Context, workerParams: WorkerParameters) :
        Worker(appContext, workerParams) {

        override fun doWork(): Result {

            val response =
                try {
                    HttpClientService.send(
                        inputData.getString("method")!!,
                        inputData.getString("path")!!,
                        inputData.getString("json"),
                        getMetaData().lastReceivedServerDelta
                    ) ?: return retry()
                } catch (e: Exception) {
                    Log.e("CallServerWorkManager", "error while sending", e)
                    return retry()
                }

            try {
                ServerResponseHandler(appContext).handle(response)
            } catch (e: Exception) {
                Log.e("CallServerWorkManager", "error while handling response", e)
            }

            return Result.success()
        }

        private fun getMetaData(): MetaData {
            val metaDataDao = NotyDatabase.getDatabase(appContext)!!.MetaDataDao()
            val metaData = metaDataDao.get()
            return if (metaData == null) {
                metaDataDao.insert(MetaData(lastReceivedServerDelta = 0))
                metaDataDao.get()!!
            } else metaData
        }

        private fun retry() = if (runAttemptCount <= 5) Result.retry() else Result.failure()
    }

}