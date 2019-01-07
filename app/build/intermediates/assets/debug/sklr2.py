import os
import os.path as path
import pandas as pd
import numpy as np
import pickle
#import matplotlib  
#matplotlib.use('TkAgg')   
#import matplotlib.pyplot as plt 
from scipy import stats
import tensorflow as tf
from sklearn import preprocessing, metrics
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from tensorflow.python.tools import freeze_graph
from tensorflow.python.tools import optimize_for_inference_lib

EXPORT_DIR = './model'

learning_rate = 0.1
training_epochs = 250
batch_size = 250
display_step = 1
MODEL_NAME = 'lrmodel'
    

columns = ['score', 'steps', 'calls', 'calltime', 'messages', 'media', 'camera', 'social', 'browser'] #NB add browser as an 8th?
dt = pd.read_csv('retail_data.csv', header=0, index_col='CustomerID')
dt.head()
dt.info()


npArray = np.array(dt)
print(dt.shape)

#data splitting and preprocessing time
xvals = npArray[:,:-1].astype(float)
yvals = npArray[:,-1]

print ("X dimensions:", xvals.shape)
print ("y dimensions:", yvals.shape)

le = preprocessing.LabelEncoder()
yvals  = le.fit_transform(yvals)
x_norm = preprocessing.scale(xvals)

#x_norm.reshape(1, x_norm.shape[0], x_norm.shape[1])
#print ("New X dimensions:", x_norm.shape)

XTrain, XTest, yTrain, yTest = train_test_split(x_norm, yvals, random_state=1)

log_reg = LogisticRegression(class_weight='balanced')
log_reg.fit(XTrain, yTrain)
yPred = log_reg.predict(XTest)

print (metrics.classification_report(yTest, yPred))
print ("Overall Accuracy:", round(metrics.accuracy_score(yTest, yPred),2))

yTrain_resize = np.zeros((len(yTrain),2))
yTrain_resize[np.arange(len(yTrain)),yTrain] = 1
print ("y dimensions:", yTrain_resize.shape)

yTrain = yTrain_resize

yTest_resize = np.zeros((len(yTest),2))
yTest_resize[np.arange(len(yTest)),yTest] = 1
print ("y dimensions:", yTest_resize.shape)

yTest = yTest_resize

print("train dimensions:", XTrain.shape)
print("test dimensions:", XTest.shape)
print("train label dimensions:", yTrain.shape)
print("test label dimensions:", yTest.shape)


#-----building the logistic regression model---

# placeholders
tf.reset_default_graph()

X = tf.placeholder(tf.float32, [None, 1, 10], name="input")
X = tf.reshape(X, [-1, 10])
Y = tf.placeholder(tf.float32, [None, 2])

W = tf.Variable(tf.random_normal([10, 2], mean=0.0, stddev=0.05))
b = tf.Variable(tf.zeros([2])) 

out = tf.add(tf.matmul(X, W), b)
pred = tf.sigmoid(out, name="output")

# Minimize error using cross entropy
cost = tf.reduce_mean(tf.nn.sigmoid_cross_entropy_with_logits(logits = out, labels = Y))
# Gradient Descent
optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate).minimize(cost)

train_count = len(XTrain)

#defining optimizer and accuracy
correct_prediction = tf.equal(tf.argmax(pred, 1), tf.argmax(Y, 1))

accuracy = tf.reduce_mean(tf.cast(correct_prediction, tf.float32))


#----Training the model------------------------------------------
saver = tf.train.Saver()

history = dict(train_loss=[], 
                train_acc=[], 
                test_loss=[], 
                test_acc=[])

sess=tf.InteractiveSession()
sess.run(tf.global_variables_initializer())

tf.train.write_graph(sess.graph_def, 'out', MODEL_NAME + '.pbtxt', True)

train_count = len(XTrain)

for i in range(1, training_epochs + 1):
    for start, end in zip(range(0, train_count, batch_size),
                          range(batch_size, train_count + 1,batch_size)):
        sess.run(optimizer, feed_dict={X: XTrain[start:end],
                                        Y: yTrain[start:end]})

    _, acc_train, loss_train = sess.run([pred, accuracy, cost], feed_dict={
                                                X: XTrain, Y: yTrain})

    _, acc_test, loss_test = sess.run([pred, accuracy, cost], feed_dict={
                                                X: XTest, Y: yTest})

    history['train_loss'].append(loss_train)
    history['train_acc'].append(acc_train)
    history['test_loss'].append(loss_test)
    history['test_acc'].append(acc_test)

    if i != 1 and i % 10 != 0:
        continue

    print(f'epoch: {i} test accuracy: {acc_test} loss: {loss_test}')

    
predictions, acc_final, loss_final = sess.run([pred, accuracy, cost], feed_dict={X: XTest, Y: yTest})

saver.save(sess, 'out/' + MODEL_NAME + '.chkp')

print()
print(f'final results: accuracy: {acc_final} loss: {loss_final}')

#-----Storing model to disk-----------------------------------------
input_node_name = 'input'
output_node_name = 'output'

if not path.exists('out'):
        os.mkdir('out')

freeze_graph.freeze_graph('out/' + MODEL_NAME + '.pbtxt', None, False,
    'out/' + MODEL_NAME + '.chkp', output_node_name, "save/restore_all",
    "save/Const:0", 'out/frozen_' + MODEL_NAME + '.pb',  clear_devices=True, initializer_nodes="")

input_graph_def = tf.GraphDef()
with tf.gfile.Open('out/frozen_' + MODEL_NAME + '.pb', "rb") as f:
    input_graph_def.ParseFromString(f.read())

output_graph_def = optimize_for_inference_lib.optimize_for_inference(
        input_graph_def, [input_node_name], [output_node_name],
        tf.float32.as_datatype_enum)

with tf.gfile.FastGFile('out/opt_' + MODEL_NAME + '.pb', "wb") as f:
    f.write(output_graph_def.SerializeToString())

print("graph saved!")

    


#-----Storing model to disk-----------------------------------------
