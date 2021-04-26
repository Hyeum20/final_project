import sqlite3

connection = sqlite3.connect('database.db')

with open('schema.sql') as f:
    connection.executescript(f.read())

cur = connection.cursor()

cur.execute("INSERT INTO posts (title, content, numberr) VALUES(?,?,?)",
        ('First Post','Content for the first post', '2')
        )

cur.execute("INSERT INTO posts (title, content, numberr) VALUES(?,?,?)",
        ('Secont Post','content for the second post', '3')
        )

connection.commit()
connection.close()
