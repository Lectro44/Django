from django.db import models

# Create your models here

class Branch(models.Model):
    sbranch = models.CharField(max_length=60)
    def __str__(self):
        return self.sbranch

class Student(models.Model):
    name = models.CharField(max_length=60)
    phno = models.BigIntegerField()
    branch = models.ForeignKey(Branch,on_delete=models.CASCADE)
    semester = models.IntegerField(default=1)
    password = models.CharField(max_length=40)
    def __str__(self):
        return self.name



class Books(models.Model):
    name = models.CharField(max_length=60)
    author = models.CharField(max_length=60)
    branch = models.ForeignKey(Branch,on_delete=models.CASCADE)
    def __str__(self) -> str:
        return self.name

class BooksAssigned(models.Model):
    sname = models.ForeignKey(Student,on_delete=models.CASCADE)
    bname = models.ForeignKey(Books,on_delete=models.CASCADE)
    startDate = models.DateField()
    endDate = models.DateField()
    def __str__(self) -> str:
        return self.sname

class StudLog(models.Model):
    neme = models.CharField(max_length=60)

    def __str__(self):
        return self.neme

    

