import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject

class DeleteWithBodyRequest(
    url: String,
    private val requestBody: JSONObject,
    private val listener: Response.Listener<JSONObject>,
    errorListener: Response.ErrorListener
) : JsonObjectRequest(Method.DELETE, url, requestBody, listener, errorListener) {

    override fun getBody(): ByteArray {
        return requestBody.toString().toByteArray()
    }

    override fun getBodyContentType(): String {
        return "application/json; charset=utf-8"
    }
}