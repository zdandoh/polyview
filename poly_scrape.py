import requests
from http import cookiejar

jar = cookiejar.CookieJar()

def dw(string):
    f = open("test.html", "w")
    f.write(string)
    f.close()

def pp(req):
    print('{}\n{}\n{}\n\n{}'.format(
        '-----------START-----------',
        req.method + ' ' + req.url,
        '\n'.join('{}: {}'.format(k, v) for k, v in req.headers.items()),
        req.body,
    ))

sess = requests.Session()

resp = sess.get(
    "https://idp.calpoly.edu/idp/profile/cas/login?service=https://myportal.calpoly.edu/Login"
    , cookies=jar, verify=False)

resp2 = sess.post(resp.url,
    data={
        "j_username": "USERNAME",
        "j_password": "PASSWORD",
        "_eventId_proceed": ""
    },
    headers={
        "Referer": resp.url,
        "User-Agent": "Mozilla/5.0",
        "Upgrade-Insecure-Requests": "1",
        "Host": "idp.calpoly.edu",
        "Cache-Control": "max-age=0",
        "Origin": "https://idp.calpoly.edu",
        "Accept-Language": "en-US,en;q=0.9",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
    }, cookies=jar, verify=False
)

print(sess.get("https://myportal.calpoly.edu/f/u17l1s6/p/myclasses.u17l1n1696/normal/moodleLinks.resource.uP?terms=2188%2Cmisc&request.preventCache=1538619342920").text)