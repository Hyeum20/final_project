import sqlite3
from flask import Flask, render_template, request, url_for, flash, redirect
from werkzeug.exceptions import abort
import socket

def get_db_connection():
    conn = sqlite3.connect('database.db')
    conn.row_factory = sqlite3.Row
    return conn
    
def get_post(post_id):
    conn = get_db_connection()
    post = conn.execute('SELECT * FROM posts WHERE id = ?',
                        (post_id,)).fetchone()
    conn.close()

    products = post['title']
    logistics = None
    if products == '그릇' :
        logistics = 'bowl'
    elif products == '테디베어':
        logistics = 'Teddy Bear'
    elif products == '컵':
        logistics = 'cup'

    sendTurtleBot(logistics)

    if post is None:
        abort(404)
    return post
    
    
app = Flask(__name__)
app.config['SECRET_KEY'] = 'your secret key'
    
@app.route('/')
def index():
    conn = get_db_connection()
    posts = conn.execute('SELECT * FROM posts').fetchall()
    conn.close()
    return render_template('index.html', posts=posts)
    
@app.route('/<int:post_id>')
def post(post_id):
    post = get_post(post_id)
    return render_template('post.html', post=post)

    
@app.route('/create', methods=('GET', 'POST'))
def create():
    if request.method == 'POST':
        title = request.form['title']
        content = request.form['content']
        numberr = request.form['numberr']

        if not title:
            flash('Title is required!')
        else:
            conn = get_db_connection()
            conn.execute('INSERT INTO posts (title, content, numberr) VALUES (?, ?, ?)',
                         (title, content, numberr))
            conn.commit()
            conn.close()
            return redirect(url_for('index'))
    return render_template('create.html')


@app.route('/minuspd/<int:id>', methods=('GET', 'POST'))
def minuspd(id):
    conn = get_db_connection()
    post = conn.execute('SELECT * FROM posts WHERE id = ?',(id,)).fetchone()
    count = post['numberr']
    print(count)
    conn.execute('UPDATE posts SET numberr =?'' WHERE id = ?',( int(count)-1, id))

    posts = conn.execute('SELECT * FROM posts').fetchall()
    conn.commit()
    conn.close()
    return render_template('index.html', posts=posts)




@app.route('/<int:id>/edit', methods=('GET', 'POST'))
def edit(id):
    post = get_post(id)

    if request.method == 'POST':
        title = request.form['title']
        content = request.form['content']
        numberr = request.form['numberr']

        if not title:
            flash('Title is required!')
        else:
            conn = get_db_connection()
            conn.execute('UPDATE posts SET title = ?, content = ?, numberr =?'
                         ' WHERE id = ?',
                         (title, content, numberr, id))
            conn.commit()
            conn.close()
            return redirect(url_for('index'))

    return render_template('edit.html', post=post)
    
@app.route('/<int:id>/delete', methods=('POST',))
def delete(id):
    post = get_post(id)
    conn = get_db_connection()
    conn.execute('DELETE FROM posts WHERE id = ?', (id,))
    conn.commit()
    conn.close()
    flash('"{}" was successfully deleted!'.format(post['title']))
    return redirect(url_for('index'))


def sendTurtleBot(logistics):
    if logistics != None :
        HOST = 'xxx.xxx.xxx.xxx'
        PORT = 0000
        data = '[flask]:'+logistics
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((HOST, PORT))
        client_socket.sendall(data.encode())
        # 소켓을 닫습니다.
        client_socket.close()


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=0000, debug=True)
    #app.run()