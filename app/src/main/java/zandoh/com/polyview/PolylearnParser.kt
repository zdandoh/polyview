package zandoh.com.polyview

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.select.Elements

fun parsePolylearn(source: String): ArrayList<Category> {
    val categories = arrayListOf<Category>()

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
            categories.add(parseCategory(curr_category))
        }
        category_no++
    }

    return categories
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
        newCat.items.add(newItem)
    }

    return newCat
}

data class Category(var title: String, val items: ArrayList<PolylearnItem>)

data class PolylearnItem(var title: String, var type: String, var description: String, var url: String)
