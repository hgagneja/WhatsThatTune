# getfp/urls.py
from django.conf.urls import url
from getfp import views

urlpatterns = [
    url(r'^$', views.HomePage.as_view()),
    url(r'^result', views.AudioSample_upload.as_view()),
    url(r'^fingerprint', views.Audio_upload.as_view()),
    url(r'^about/$', views.AboutPage.as_view()),
    url(r'^services/$', views.ServicesPage.as_view()),
    url(r'^contact/$', views.ContactPage.as_view()),
]
