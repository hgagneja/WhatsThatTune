from django.shortcuts import render
from django.views.generic import TemplateView
from django.views.decorators.csrf import csrf_exempt
from wttune.record_audio import audio_sample
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from django.http import Http404
import os
import errno
import contextlib
import requests
import spotipy


class HomePage(APIView):
    def get(self, request, **kwargs):
        return render(request, 'main.html', context=None)

    def post(self, request, **kwargs):
        #audio_sample_file = request.FILES['record']
        filepath = os.getcwd()
        #destination = open(filepath + '/' + audio_sample_file.name, 'wb+')
        #with open(filepath + '/' + audio_sample_file.name, 'wb+') as destination:
        #    for chunk in audio_sample_file.chunks():
        #        destination.write(chunk)

	spotify = spotipy.Spotify()
        url = "http://35.163.12.69:81/result"
        headers = {
              "Content-Type": "multipart/form-data"
        }
        
        record_init = audio_sample()
        record_file = record_init.get_audio_inp()
        #record_file = filepath + '/' + audio_sample_file.name
        with open(record_file, 'rb') as f:
            try:
                response = requests.post(url, files={'file': f})
                print(response.text)
                resp_1 = response.text[1:-1]
                resp = resp_1.split(",")
            except requests.exceptions.RequestException as exc:
                print(str(exc))
                #raise WebServiceError("HTTP request failed: {0}".format(exc))

        if isinstance(response.text,basestring) and (len(resp) > 1) :
                name = resp[1]
                name =  name.replace("-", "+")
                sp_link = ''
                results = spotify.search(q='track:' + name, type='track')
                items = results['tracks']['items']
                if len(items) > 0:
                   artist = items[0]
                   sp_link = artist['external_urls']['spotify']
                   print artist['name'], artist['external_urls']['spotify']
                return render(request, 'index.html', {'message_name': resp[1],'message_artist' : resp[2], 'message_album': resp[0], 'spotify_link': sp_link } )
        else:
                return render(request, 'index.html', {'message_name': "NULL",'message_artist' : "NULL", 'message_album': "NULL" } )





class AboutPage(TemplateView):
    template_name = "about.html"


class ServicesPage(TemplateView):
    template_name = "services.html"



class ContactPage(TemplateView):
    template_name = "contact.html"

