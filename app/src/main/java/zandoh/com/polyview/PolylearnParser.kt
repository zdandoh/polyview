package zandoh.com.polyview

import android.os.Parcelable
import android.util.Log
import kotlinx.android.parcel.Parcelize
import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun parsePolylearn(source: String): PolylearnData {
    val data = PolylearnData()

    val doc = Jsoup.parse(source)

    var category_no = 0
    var curr_category: Elements

    while(true) {
        curr_category = doc.select("#section-$category_no")
        if(curr_category.isEmpty()) {
            // All categories have been consumed
            break
        }
        else {
            data.categories.add(parseCategory(curr_category))
        }
        category_no++
    }

    return data
}

fun parseCategory(category: Elements): Category {
    val newCat = Category("", arrayListOf())

    newCat.title = category.attr("aria-label")
    var activities = category.select(".activity")
    for(activity in activities) {
        val activityLink = activity.select(".activityinstance").select("a")
        if(activityLink.isEmpty()) {
            continue
        }

        val newItem = PolylearnItem(
                activityLink.select(".instancename").first().ownText(),
                activityLink.select(".accesshide").text(),
                "",
                activityLink.attr("href")
        )

        if(newItem.type == "") {
            val activityName = activityLink.select("img").attr("src").split("/").reversed()[2]
            newItem.type = activityName
        }

        newItem.type = newItem.type.toUpperCase()


        newCat.items.add(newItem)
    }

    return newCat
}

@Parcelize
data class PolylearnDataHolder(val items: HashMap<String, PolylearnData> = HashMap()): Parcelable

@Parcelize
data class PolylearnData(var categories: ArrayList<Category> = arrayListOf(), var ctime: Long = System.currentTimeMillis() / 1000): Parcelable {
    fun get(index: Int): Any? {

        var currIndex = 0
        for(category in categories) {
            if(index == currIndex) {
                return category
            }


            if(currIndex + category.items.size >= index) {
                // The item we're looking for is in this category
                return category.items[index - currIndex - 1]
            }
            else {
                currIndex += category.items.size + 1
            }
        }

        return null
    }
}



@Parcelize
data class Category(var title: String, val items: ArrayList<PolylearnItem>): Parcelable

@Parcelize
data class PolylearnItem(
        var title: String,
        var type: String,
        var description: String,
        var url: String): Parcelable
