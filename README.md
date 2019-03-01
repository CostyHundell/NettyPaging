# NettyPager
A library helper that remove boilerplate and make it easy to have paged data from multiple different API calls so to the user it looks like one.

## Adding the dependency

Add jitpack as a dependency to your project gradle
```
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            url 'https://jitpack.io'
        }
    }
}
```

Then add the library to your app gradle
```
implementation 'com.github.CostyHundell:NettyPager:1.1.0-alpha1'
```

## Using the library

Hopefully you have a basic knowledge of the PagedListAdapter and how to set this up if not [this article](https://proandroiddev.com/8-steps-to-implement-paging-library-in-android-d02500f7fffe) by Anitaa Murthy is a good starter. You will need to use retrofit2 and retrofitRxJava.

```
implementation 'com.squareup.retrofit2:retrofit:2.3.0'
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.3.0'
implementation 'com.squareup.retrofit2:converter-gson:2.3.0'
implementation 'io.reactivex.rxjava2:rxjava:2.1.12'
implementation 'io.reactivex.rxjava2:rxandroid:2.0.2'
```

Instead of using `PagedKeyDataSource` in that example you use the libraries new class `NettyPagedDataSource`.

And you should then get something like this:
```
class TestDataSource: NettyPagerDataSource(NUMBER_OF_APIS) {
    override var single: Single<NettyResponse>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
        set(value) {}

    override fun onLoadAfterError(error: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadAfterSuccess(
        callback: LoadCallback<Int, NettyItem>,
        response: NettyResponse,
        params: LoadParams<Int>
    ) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadInitialError(error: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLoadInitialSuccess(callback: LoadInitialCallback<Int, NettyItem>, response: NettyResponse) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
```

But there's a few things that also need to be done. As you can see we have a `NettyItem` this is an interface that you must use when creating the objects that your app is going to display in a list. For Example :
```
data class NewsArticle(val source: String,
                        val author: String,
                        val title: String,
                        val description: String,
                        val url: String,
                        val imageUrl: String,
                        val publishedAt: String,
                        val content: String): NettyItem {
    // Must have unique viewType
    override fun getItemViewType(): Int = Constant.NEWS_ARTICLE_TYPE 
}

data class Advert(val id: String,
                  val url: String): : NettyItem {
    // Must have unique viewType
    override fun getItemViewType(): Int = Constant.AD_VIEW_TYPE 
```
As well as doing the same for the Api response except implementing `NettyResponse`:
```
data class NewsResponse(val status: String,
                        val totalResults: Int,
                        val articles: List<NewsArticle>) : NettyResponse {
    override fun getResponseType(): Int = Constant.NEWS_RESPONSE
}
```

You will need to register a type adapter in your `GsonConverterFactory` you can implement your own but if your response is a json object just create the adapter with a call like this:
```
val adapter = DeserializeResponse(NewsResponse::class.java)
```

So what this now allows you to do is call two different APIs that get two different responses but once finished for the user it will look like one list.

Note: This means that your RetrofitAPI call should return a `NettyResponse` for all API calls not the response you're looking for.

The first thing to do is set the initial `single` variable.
```
override var single = NewsRetrofitApi().getArticles()
```
Then in `onLoadInitialSuccess()` we deal with the response

```
var list = mutableListOf<NettyItem>()

    override fun onLoadInitialSuccess(callback: LoadInitialCallback<Int, NettyItem>, response: NettyResponse) {
         when (response.getResponseType()) {
            Constant.NEWS_RESPONSE -> {
                response as NewsResponse
                list = response.articles.toMutableList()
            }
            ...
        }


        if (callsMade == NUMBER_OF_APIS) {
            postInitial(callback, list, PAGE_SIZE + 1)
        } else {
            setNextInitialCall(true, PAGE_SIZE, manageApis(), callback).runNext()
        }
    }
```

The `callsMade` variable increments every time a new API call is made and is reset once we have made the amount of calls defined in the constructor.

Above you can see that we have a variable `list` and if it's the first call we just set that to the list of articles from the response. If it is not the first call we add the result to the original list and then shuffle the whole list. You can do what ever you want with your results this is just an example. The same happens for `onLoadAfterSuccess()`. 

`manageMyApis()` is a method that if not overriden will return null. It does not have to be used as you can build your own method to determine which api to use. But this is the idea:
```
override fun manageApis(): Single<Response> = when (callsMade) {
      1 -> NewsRetrofitApi().getArticles()
      else -> AdsRetroFit().getAds()
  }
```

And then once you've implemented this, run the app and you should see your list of different feeds look like on normal feed. And it will be done with ease.
