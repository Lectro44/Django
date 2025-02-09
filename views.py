from django.shortcuts import render,redirect
from django.http import HttpResponse
from django.contrib.auth.models import User
from django.contrib import messages
from django.contrib.auth import login,logout,authenticate
from .models import Student,Branch,Books,BooksAssigned,StudLog
from django.contrib.auth.decorators import login_required
from django.contrib.auth.hashers import make_password

profile = ""

# Create your views here.
def base(request):
    return render(request,'index.html')

def adm_signup(request):
    return render(request,'admin_signup.html')


def regis_admin(request):
    adm = User()
    if request.method == "POST":
        
        try:
            username = request.POST['username']
            pwd = request.POST['password']
            eml = request.POST['email']
            if username in adm.username:
                messages.error(request,'Username already exists. Try again with another username.')
                return redirect('/adm_signup')
            else:
                u1 = User.objects.create_user(username=username,password=pwd,email=eml)
                u1.save()
                return render(request,'common_signin.html')
        except:
            messages.error(request,'Username could not be verified. Try again')
            return redirect('/adm_signup')


def stud_signup(request):
    brc = Branch.objects.all()
    return render(request,'student.html',{'branches':brc})

def regis_stud(request):
    name = request.POST['sname']
    phno = request.POST['sphno']
    branc = request.POST['sbranch']
    sem = request.POST['semester']
    pwd = request.POST['password']
    b1 = Branch.objects.get(sbranch = branc)
    s1 = Student(branch=b1)
    s1.name = name
    s1.phno = phno
    s1.semester = sem
    s1.password = pwd
    s1.save()
    return redirect('/com_signin')

def com_signin(request):
    return render(request,'common_signin.html')

@login_required
def admin_dash(request):
    if request.user.is_authenticated:
        return render(request,'admin_base.html')
    else:
        return redirect('/com_signin')

def stud_dash(request):
    global profile
    stude = StudLog.objects.get(id = profile)
    st1 = Student.objects.all()
    for student in st1:
        if stude.neme in st1.name:
            return render(request,'student_base.html',{'logs':stude,'students':st1})



def user_auth(request):
    if request.method == "POST":
        username = request.POST['name']
        password = request.POST['password']
        user = authenticate(request, username=username, password=password)
        
        if user is not None:
            login(request,user)
            return redirect('/admin_dash')
        if user is None:
            student = Student.objects.get(name=username)
            if student.name == username and student.password == password:
                global profile
                profile = StudLog.objects.create(neme = student.name )
                profile.save()

                return render(request,'student_base.html',{'students':student,'profile':profile})
            else:
                return render(request,'common_signin.html')
    else:
        messages.error(request,'Something went wrong')
        return redirect('/')

def log_out(request):
    logout(request)
    return redirect('/')

# def stud_logut(request):
    
#     stud = Student.objects.all()
#     logut = StudLog.objects.get(id = stud.name)
#     logut.delete()
#     return redirect('/')


def books(request):
    branc = Branch.objects.all()
    return render(request,'admin_add.html',{'branches':branc})

def add_books(request):
    bname = request.POST['bkname']
    bauth = request.POST['auname']
    bbranc = request.POST['bbranch']
    br1 = Branch.objects.get(sbranch = bbranc)
    
    bk1 = Books.objects.create(
        name = bname,
        author = bauth,
        branch = br1
    )
    bk1.save()
    return render(request,'display.html')

def show_books(request):
    bk1 = Books.objects.all()
    return render(request,'admin_books.html',{'books':bk1})

def update_books(request,id):
    books = Books.objects.get(id = id)
    bk_br = Branch.objects.all()
    return render(request,'admin_update.html',{'bookers': books,'branches':bk_br})

def update_stud(request,id):
    newname = request.POST['bkname']
    newauth = request.POST['auname']
    newbranch = request.POST['bbranch']
    bran = Branch.objects.get(sbranch=newbranch)
    bk4 = Books.objects.get(id = id)
    bk4.name = newname
    bk4.author = newauth
    bk4.branch = bran
    bk4.save()
    return redirect('/show_books') 

    

def delete_book(request,id):
    books = Books.objects.get(id=id)
    books.delete()
    return redirect('/show_books')



def asign(request):
    stud = Student.objects.all()
    bk = Books.objects.all()
    return render(request,'admin_assign.html',{'names':stud,'books':bk})

def assign_books(request):
    name = request.POST['sname']
    bkname = request.POST['bname']
    start = request.POST['sdate']
    end = request.POST['edate']
    st1 = Student.objects.get(name=name)
    bk1 = Books.objects.get(name = bkname)

    ba1 = BooksAssigned.objects.create(
        sname = st1,
        bname = bk1,
        startDate = start,
        endDate = end
    )
    ba1.save()
    return render(request,'disp_bkassign.html')

def show_assign(request):
    bk_name = BooksAssigned.objects.all() 
    return render(request,'admin_assigned.html',{'names':bk_name})

def update_stbook(request,id):
    book = BooksAssigned.objects.get(id = id)
    return render(request,'admin_updated.html',{'books':book})

def updating_stbook(request,id):
    newname = request.POST['name']
    newbook = request.POST['bkname']
    st5 = Student.objects.get(name = newname)
    book = Books.objects.get(name = newbook)
    bk5 = BooksAssigned.objects.get(id = id)
    bk5.sname = st5
    bk5.bname = book
    bk5.save()
    return redirect('/show_assign')

def delete_name(request,id):
    names = BooksAssigned.objects.get(id = id)
    names.delete()
    return redirect('/show_assign')

def stud_prof(request,id):
    # studentid=request.id(students)
    stud = Student.objects.get(id = id)
    return render(request,'student_prof.html',{'students':stud})

def prof_template(request,id):
    studd = Student.objects.get(id = id)
    brn = Branch.objects.all()
    return render(request,'student_update.html',{'studen':studd,'branches':brn})


def update_prof(request,id):
    newname = request.POST['sname']
    newphno = request.POST['sphno']
    newbranch = request.POST['sbranch']
    newsem = request.POST['semester']
    newpass = request.POST['password']

    b1 = Branch.objects.get(sbranch = newbranch)

    s1 = Student.objects.get(id = id)
    s1.name = newname
    s1.phno = newphno
    s1.branch = b1
    s1.semester = newsem
    s1.password = newpass
    s1.save()
    return redirect('/stud_prof/{{ s1.id }}')

def borrowed(request):
    books_assign = BooksAssigned.objects.all()
    return render(request,'student_books.html',{'books':books_assign})

def stud_books(request,id):
    books = BooksAssigned.objects.get(id = id)
    books.delete()
    return redirect('/borrowed')






